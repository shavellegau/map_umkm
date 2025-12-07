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
import com.google.android.material.chip.ChipGroup // 🔥 Wajib Import ini
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminOrdersFragment : Fragment() {

    // UI Components
    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter

    // 🔥 UI Filter
    private lateinit var chipGroupFilter: ChipGroup

    // UI Statistik
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalUsers: TextView

    // Firebase & Data
    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null

    // Variable untuk Filter
    private var allOrderList: List<Order> = emptyList() // Menyimpan semua data asli
    private var currentFilterMode = "ALL" // Default filter

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

        // 1. Inisialisasi View
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)

        // 🔥 Inisialisasi ChipGroup (Pastikan ID di XML sudah benar)
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter)

        tvTotalOrders = view.findViewById(R.id.tvTotalOrders)
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts)
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)

        setupRecyclerView()

        // Setup Listener untuk Filter Chip
        setupFilterListener()

        return view
    }

    override fun onResume() {
        super.onResume()
        startListeningForOrders()
        startListeningForStats() // 🔥 Memanggil fungsi statistik
    }

    override fun onPause() {
        super.onPause()
        orderListener?.remove()
        statsListener1?.remove()
        statsListener2?.remove()
        statsListener3?.remove()
    }

    // --------------------------------------------------------
    // 🔥 1. FUNGSI LOAD ORDER & FILTERING
    // --------------------------------------------------------
    private fun startListeningForOrders() {
        orderListener?.remove()

        orderListener = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error load orders: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // Simpan data asli ke variabel global
                    allOrderList = snapshots.toObjects(Order::class.java)

                    // Terapkan filter saat ini (misal: ALL atau TODAY)
                    applyFilter()
                }
            }
    }

    private fun setupFilterListener() {
        chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentFilterMode = when (checkedIds[0]) {
                    R.id.chipToday -> "TODAY"
                    R.id.chipYesterday -> "YESTERDAY"
                    R.id.chipWeek -> "WEEK"
                    else -> "ALL" // chipAll
                }
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filteredList = when (currentFilterMode) {
            "TODAY" -> allOrderList.filter { isDateToday(it.orderDate) }
            "YESTERDAY" -> allOrderList.filter { isDateYesterday(it.orderDate) }
            "WEEK" -> allOrderList.filter { isDateWithinLastWeek(it.orderDate) }
            else -> allOrderList
        }

        if (filteredList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvRecentOrders.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRecentOrders.visibility = View.VISIBLE
            adapter.updateData(filteredList)
        }
    }

    // --------------------------------------------------------
    // 🔥 2. HELPER TANGGAL
    // --------------------------------------------------------
    private fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        return try {
            // Format harus sama persis dengan yang disimpan di database
            // Contoh: "07 Desember 2025, 12:00"
            val format = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun isDateToday(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val now = Calendar.getInstance()
        val orderDate = Calendar.getInstance().apply { time = date }

        return now.get(Calendar.YEAR) == orderDate.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == orderDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateYesterday(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val orderDate = Calendar.getInstance().apply { time = date }

        return yesterday.get(Calendar.YEAR) == orderDate.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == orderDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateWithinLastWeek(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val oneWeekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        return date.after(oneWeekAgo.time)
    }

    // --------------------------------------------------------
    // 🔥 3. FUNGSI STATISTIK (Mengatasi error Unresolved Reference)
    // --------------------------------------------------------
    private fun startListeningForStats() {
        // Stats 1: Pesanan Baru
        statsListener1?.remove()
        statsListener1 = db.collection("orders")
            .whereIn("status", listOf("Menunggu Konfirmasi", "Menunggu Pembayaran"))
            .addSnapshotListener { snapshots, _ ->
                tvTotalOrders.text = (snapshots?.size() ?: 0).toString()
            }

        // Stats 2: Diproses
        statsListener2?.remove()
        statsListener2 = db.collection("orders")
            .whereEqualTo("status", "Diproses")
            .addSnapshotListener { snapshots, _ ->
                tvTotalProducts.text = (snapshots?.size() ?: 0).toString()
            }

        // Stats 3: Selesai
        statsListener3?.remove()
        statsListener3 = db.collection("orders")
            .whereEqualTo("status", "Selesai")
            .addSnapshotListener { snapshots, _ ->
                tvTotalUsers.text = (snapshots?.size() ?: 0).toString()
            }
    }

    // --------------------------------------------------------
    // 🔥 4. UPDATE STATUS & NOTIFIKASI
    // --------------------------------------------------------
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
                if (!token.isNullOrEmpty()) {
                    // Kirim orderId agar masuk Tab Info di user
                    fcmService.sendNotification(token, title, body, orderId)
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
}