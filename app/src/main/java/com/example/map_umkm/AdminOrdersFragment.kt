package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AdminOrdersAdapter
import com.example.map_umkm.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

class AdminOrdersFragment : Fragment() {

    // UI Daftar Pesanan
    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter
    // HAPUS: fabAction (FloatingActionButton) sudah dihapus

    // UI Statistik
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalUsers: TextView

    // Firebase & Listener
    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null

    // Listener Statistik
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
        // HAPUS: Inisialisasi fabAction dihapus

        // 2. Inisialisasi Statistik
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders)
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts)
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)

        setupRecyclerView()
        // HAPUS: Listener fabAction.setOnClickListener dihapus

        return view
    }

    override fun onResume() {
        super.onResume()
        // Mulai mendengarkan data
        startListeningForOrders()
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
                    // Cek if context null sebelum toast untuk menghindari crash saat fragment detach
                    context?.let { Toast.makeText(it, "Gagal memuat daftar pesanan.", Toast.LENGTH_LONG).show() }
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

    // ---------------- FUNGSI LOAD STATISTIK ------------------
    private fun startListeningForStats() {
        // 1. Pesanan Baru (Menunggu Konfirmasi / Menunggu Pembayaran)
        statsListener1?.remove()
        statsListener1 = db.collection("orders")
            .whereIn("status", listOf("Menunggu Konfirmasi", "Menunggu Pembayaran"))
            .addSnapshotListener { snapshots, _ ->
                val count = snapshots?.size() ?: 0
                tvTotalOrders.text = count.toString()
            }

        // 2. Diproses
        statsListener2?.remove()
        statsListener2 = db.collection("orders")
            .whereEqualTo("status", "Diproses")
            .addSnapshotListener { snapshots, _ ->
                val count = snapshots?.size() ?: 0
                tvTotalProducts.text = count.toString()
            }

        // 3. Selesai
        statsListener3?.remove()
        statsListener3 = db.collection("orders")
            .whereEqualTo("status", "Selesai")
            .addSnapshotListener { snapshots, _ ->
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
                Toast.makeText(context, "Status: $newStatus", Toast.LENGTH_SHORT).show()
                // Kirim notifikasi personal ke user terkait pesanan ini
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

    // HAPUS: Fungsi showPromoDialog() telah dihapus sepenuhnya dari sini.
}