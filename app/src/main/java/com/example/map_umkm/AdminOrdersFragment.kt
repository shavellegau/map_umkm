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
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminOrdersFragment : Fragment() {


    // UI
    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter
    private lateinit var chipGroupFilter: ChipGroup

    // Statistik
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalUsers: TextView

    // Data
    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null

    // Variabel Filter
    private var allOrderList: List<Order> = emptyList() // Data mentah
    private var currentFilterMode = "ALL" // Default mode

    // Listener Statistik
    private var statsListener1: ListenerRegistration? = null
    private var statsListener2: ListenerRegistration? = null
    private var statsListener3: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)
        fcmService = FCMService(requireContext())

        // Init UI
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter)

        tvTotalOrders = view.findViewById(R.id.tvTotalOrders)
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts)
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)

        setupRecyclerView()

        // Setup Listener Chip
        setupFilterListener()

        return view
    }

    override fun onResume() {
        super.onResume()
        startListeningForOrders()
        startListeningForStats()
    }

    override fun onPause() {
        super.onPause()
        orderListener?.remove()
        statsListener1?.remove()
        statsListener2?.remove()
        statsListener3?.remove()
    }

    // ---------------- LOGIKA FILTER ------------------
    private fun setupFilterListener() {
        // Pastikan default terpilih
        chipGroupFilter.check(R.id.chipAll)

        chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentFilterMode = when (checkedIds[0]) {
                    R.id.chipToday -> "TODAY"
                    R.id.chipYesterday -> "YESTERDAY"
                    R.id.chipWeek -> "WEEK"
                    else -> "ALL"
                }
                applyFilter() // Terapkan filter setiap kali chip berubah
            } else {
                // Jika user uncheck (kosong), paksa kembali ke ALL agar tidak blank
                currentFilterMode = "ALL"
                chipGroupFilter.check(R.id.chipAll)
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        // Logika Filter
        val filteredList = when (currentFilterMode) {
            "TODAY" -> allOrderList.filter { isDateToday(it.orderDate) }
            "YESTERDAY" -> allOrderList.filter { isDateYesterday(it.orderDate) }
            "WEEK" -> allOrderList.filter { isDateOlderThanWeek(it.orderDate) }
            else -> allOrderList // Jika ALL, kembalikan semua data
        }

        // Update UI
        if (filteredList.isEmpty()) {
            tvEmpty.text = if (allOrderList.isEmpty()) "Belum ada pesanan." else "Tidak ada pesanan untuk filter ini."
            tvEmpty.visibility = View.VISIBLE
            rvRecentOrders.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRecentOrders.visibility = View.VISIBLE
            adapter.updateData(filteredList)
        }
    }

    // ---------------- HELPER DATE ------------------
    private fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        return try {
            val format = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            format.parse(dateString)
        } catch (e: Exception) { null }
    }

    private fun isDateToday(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateYesterday(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val target = Calendar.getInstance().apply { time = date }
        return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateOlderThanWeek(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        return date.before(weekAgo.time)
    }

    // ---------------- FIREBASE LOAD ------------------
    private fun startListeningForOrders() {
        orderListener?.remove()
        orderListener = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // 1. Simpan semua data mentah
                    allOrderList = snapshots.toObjects(Order::class.java)

                    // 2. Langsung terapkan filter (PENTING AGAR TIDAK KOSONG SAAT AWAL LOAD)
                    applyFilter()
                }
            }
    }

    // ---------------- STATISTIK & UPDATE ------------------
    private fun startListeningForStats() {
        statsListener1?.remove()
        statsListener1 = db.collection("orders")
            .whereIn("status", listOf("Menunggu Konfirmasi", "Menunggu Pembayaran"))
            .addSnapshotListener { s, _ -> tvTotalOrders.text = "${s?.size() ?: 0}" }

        statsListener2?.remove()
        statsListener2 = db.collection("orders")
            .whereEqualTo("status", "Diproses")
            .addSnapshotListener { s, _ -> tvTotalProducts.text = "${s?.size() ?: 0}" }

        statsListener3?.remove()
        statsListener3 = db.collection("orders")
            .whereEqualTo("status", "Selesai")
            .addSnapshotListener { s, _ -> tvTotalUsers.text = "${s?.size() ?: 0}" }
    }

    private fun updateStatusAndNotify(
        orderId: String, newStatus: String, token: String?, title: String, body: String, targetEmail: String?
    ) {
        db.collection("orders").document(orderId).update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Status: $newStatus", Toast.LENGTH_SHORT).show()
                if (!token.isNullOrEmpty()) {
                    // Kirim orderId dan targetEmail ke FCMService
                    fcmService.sendNotification(token, title, body, orderId, targetEmail)
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter(
            emptyList(),
            onItemClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                startActivity(intent)
            },
            // Pass order.userEmail agar notifikasi masuk ke akun yang benar
            onConfirmPaymentClick = { order -> updateStatusAndNotify(order.orderId, "Diproses", order.userToken, "Pesanan Diproses", "Pembayaran sedang dicek Admin.", order.userEmail) },
            onProsesClick = { order ->
                // Opsional: Jika ada logika khusus tombol proses
            },
            onAntarPesananClick = { order -> updateStatusAndNotify(order.orderId, "Dikirim", order.userToken, "Pesanan Dikirim", "Pesananmu sedang dalam perjalanan.", order.userEmail) },
            onSelesaikanClick = { order -> updateStatusAndNotify(order.orderId, "Selesai", order.userToken, "Pesanan Selesai", "Terima kasih sudah memesan!", order.userEmail) }
        )

        rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        rvRecentOrders.adapter = adapter
    }
}