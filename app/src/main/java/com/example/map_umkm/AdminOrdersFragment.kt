package com.example.map_umkm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log // Tambahkan Log untuk debugging
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AdminOrdersAdapter
import com.example.map_umkm.model.Order
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

class AdminOrdersFragment : Fragment() {

    // UI Daftar Pesanan
    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter
    private lateinit var fabAction: FloatingActionButton

    // 🔥 VARIABEL UI STATISTIK BARU (sesuai ID di XML Anda) 🔥
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalProducts: TextView // Akan digunakan untuk 'Diproses'
    private lateinit var tvTotalUsers: TextView   // Akan digunakan untuk 'Selesai'

    // Firebase & Listener
    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null
    // Anda mungkin perlu listener terpisah untuk statistik jika ingin menghentikannya secara terpisah
    private var statsListener1: ListenerRegistration? = null
    private var statsListener2: ListenerRegistration? = null
    private var statsListener3: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)

        fcmService = FCMService(requireContext())

        // 1. Inisialisasi Daftar Pesanan
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)
        fabAction = view.findViewById(R.id.fab_add_promo)

        // 2. 🔥 Inisialisasi Statistik 🔥
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders)
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts)
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)

        setupRecyclerView()
        fabAction.setOnClickListener { showPromoDialog() }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Mulai mendengarkan daftar pesanan
        startListeningForOrders()
        // 🔥 Mulai mendengarkan statistik 🔥
        startListeningForStats()
    }

    override fun onPause() {
        super.onPause()
        // Hentikan listener saat fragment tidak terlihat
        orderListener?.remove()
        statsListener1?.remove()
        statsListener2?.remove()
        statsListener3?.remove()
    }

    // ---------------- FIREBASE LOAD ORDERS (REAL-TIME) ------------------
    private fun startListeningForOrders() {
        orderListener?.remove()

        orderListener = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Order list listen failed.", e)
                    Toast.makeText(context, "Gagal memuat daftar pesanan.", Toast.LENGTH_LONG).show()
                    tvEmpty.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                val orders = snapshots?.toObjects(Order::class.java) ?: emptyList()

                if (orders.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvRecentOrders.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvRecentOrders.visibility = View.VISIBLE
                    adapter.updateData(orders)
                }
            }
    }

    // ---------------- 🔥 FUNGSI LOAD STATISTIK BARU 🔥 ------------------
    private fun startListeningForStats() {
        // 1. Pesanan Baru (Menunggu Konfirmasi / Menunggu Pembayaran)
        statsListener1?.remove()
        statsListener1 = db.collection("orders")
            .whereIn("status", listOf("Menunggu Konfirmasi", "Menunggu Pembayaran"))
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.e("Firestore", "Stats 1 failed.", e); return@addSnapshotListener }
                val count = snapshots?.size() ?: 0
                tvTotalOrders.text = count.toString()
            }

        // 2. Diproses
        statsListener2?.remove()
        statsListener2 = db.collection("orders")
            .whereEqualTo("status", "Diproses")
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.e("Firestore", "Stats 2 failed.", e); return@addSnapshotListener }
                val count = snapshots?.size() ?: 0
                tvTotalProducts.text = count.toString()
            }

        // 3. Selesai
        statsListener3?.remove()
        statsListener3 = db.collection("orders")
            .whereEqualTo("status", "Selesai")
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.e("Firestore", "Stats 3 failed.", e); return@addSnapshotListener }
                val count = snapshots?.size() ?: 0
                tvTotalUsers.text = count.toString()
            }
    }

    // ---------------- FIREBASE UPDATE STATUS ------------------
    private fun updateStatusAndNotify(
        orderId: String,
        newStatus: String,
        token: String?,
        title: String,
        body: String
    ) {
        db.collection("orders")
            .document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Status diubah menjadi $newStatus", Toast.LENGTH_SHORT).show()
                // Angka statistik dan daftar pesanan akan otomatis terupdate oleh listener
                if (!token.isNullOrEmpty()) {
                    fcmService.sendNotification(token, title, body)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- RECYCLERVIEW ------------------
    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter(
            emptyList(),
            onItemClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                startActivity(intent)
            },
            onConfirmPaymentClick = { order ->
                updateStatusAndNotify(order.orderId, "Menunggu Konfirmasi", order.userToken, "Status Pesanan", "Pembayaran sedang dicek Admin.")
            },
            onProsesClick = { order ->
                updateStatusAndNotify(order.orderId, "Diproses", order.userToken, "Pesanan Diproses", "Pesananmu sedang dibuat.")
            },
            onSelesaikanClick = { order ->
                updateStatusAndNotify(order.orderId, "Selesai", order.userToken, "Pesanan Selesai", "Silakan ambil pesananmu!")
            }
        )

        rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        rvRecentOrders.adapter = adapter
    }

    // ---------------- BROADCAST PROMO ------------------
    private fun showPromoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kirim Info Broadcast")
        builder.setMessage("Pesan ini dikirim ke semua user (topic: promo).")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputTitle = EditText(requireContext())
        inputTitle.hint = "Judul"
        layout.addView(inputTitle)

        val inputBody = EditText(requireContext())
        inputBody.hint = "Isi pesan"
        layout.addView(inputBody)

        builder.setView(layout)

        builder.setPositiveButton("Kirim") { _, _ ->
            val title = inputTitle.text.toString()
            val body = inputBody.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(context, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            fcmService.sendNotification("promo", title, body)
            Toast.makeText(context, "Broadcast terkirim!", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }
}