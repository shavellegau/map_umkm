package com.example.map_umkm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    // UI
    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter
    private lateinit var fabAction: FloatingActionButton
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalUsers: TextView

    // Firebase
    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    // OPTIMISASI: Gunakan HANYA SATU listener untuk semua data pesanan
    private var allOrdersListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)
        fcmService = FCMService(requireContext())

        // Inisialisasi UI
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)
        // Sekarang ID ini sudah ada di layout XML
        fabAction = view.findViewById(R.id.fab_add_promo)
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders)
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts)
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)

        setupRecyclerView()
        fabAction.setOnClickListener { showPromoDialog() }

        return view
    }

    override fun onResume() {
        super.onResume()
        // OPTIMISASI: Panggil fungsi listener tunggal
        startListeningForAllOrdersAndStats()
    }

    override fun onPause() {
        super.onPause()
        // OPTIMISASI: Hentikan satu listener saja
        allOrdersListener?.remove()
    }

    // OPTIMISASI: Satu fungsi untuk mengambil semua pesanan dan menghitung statistik
    private fun startListeningForAllOrdersAndStats() {
        allOrdersListener?.remove()

        allOrdersListener = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "All-in-one listener failed.", e)
                    Toast.makeText(context, "Gagal memuat data pesanan.", Toast.LENGTH_LONG).show()
                    tvEmpty.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                val orders = snapshots?.toObjects(Order::class.java) ?: emptyList()

                // Update RecyclerView
                if (orders.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvRecentOrders.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvRecentOrders.visibility = View.VISIBLE
                    adapter.updateData(orders)
                }

                // OPTIMISASI: Hitung statistik dari list 'orders' yang sudah ada di memori
                calculateStats(orders)
            }
    }

    /**
     * Fungsi baru untuk menghitung statistik dari data yang sudah ada di aplikasi.
     * Tidak melakukan query baru ke Firestore, sehingga lebih hemat & cepat.
     */
    private fun calculateStats(orders: List<Order>) {
        val newOrdersCount = orders.count { it.status == "Menunggu Konfirmasi" || it.status == "Menunggu Pembayaran" }
        val processingCount = orders.count { it.status == "Diproses" }
        val completedCount = orders.count { it.status == "Selesai" }

        tvTotalOrders.text = newOrdersCount.toString()
        tvTotalProducts.text = processingCount.toString()
        tvTotalUsers.text = completedCount.toString()
    }

    private fun updateStatusAndNotify(orderId: String, newStatus: String, token: String?, title: String, body: String) {
        db.collection("orders").document(orderId).update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Status diubah menjadi $newStatus", Toast.LENGTH_SHORT).show()
                if (!token.isNullOrEmpty()) {
                    fcmService.sendNotification(token, title, body)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        // PERBAIKAN: Menggunakan nama parameter 'orders' yang benar
        adapter = AdminOrdersAdapter(
            orders = emptyList(), // Parameter pertama adalah list pesanan
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
            // Menggunakan nama parameter yang benar: 'onAntarPesananClick'
            onAntarPesananClick = { order ->
                updateStatusAndNotify(order.orderId, "Selesai", order.userToken, "Pesanan Selesai", "Silakan ambil pesananmu!")
            }
        )

        rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        rvRecentOrders.adapter = adapter
    }

    private fun showPromoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kirim Info Broadcast")
        builder.setMessage("Pesan ini dikirim ke semua user (topic: promo).")

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val inputTitle = EditText(requireContext()).apply { hint = "Judul" }
        val inputBody = EditText(requireContext()).apply { hint = "Isi pesan" }
        layout.addView(inputTitle)
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
