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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminDeliveryFragment : Fragment() {

    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter
    private lateinit var chipGroupFilter: ChipGroup

    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null

    private var allOrderList: MutableList<Order> = mutableListOf()
    private var currentFilterMode = "ALL"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_delivery, container, false)
        fcmService = FCMService(requireContext())

        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter)

        setupRecyclerView()
        setupFilterListener()

        return view
    }

    override fun onResume() {
        super.onResume()
        startListeningForOrders()
    }

    override fun onPause() {
        super.onPause()
        orderListener?.remove()
    }

    private fun confirmPaymentAndAwardPoints(order: Order) {
        if (order.userId.isEmpty()) {
            Toast.makeText(context, "Error: User ID tidak ditemukan.", Toast.LENGTH_LONG).show()
            return
        }

        val orderRef = db.collection("orders").document(order.orderId)
        val userRef = db.collection("users").document(order.userId)

        db.runTransaction { transaction ->
            // 1. READ FIRST
            val userSnapshot = transaction.get(userRef)
            val currentPoints = userSnapshot.getLong("tukuPoints") ?: 0L

            // 2. WRITES
            transaction.update(orderRef, "status", "Diproses")

            val subtotal = order.items.sumOf { item -> (((if (item.selectedType == "iced") item.price_iced else item.price_hot) ?: 0) * item.quantity).toDouble() }
            val pointsEarned = (subtotal * 0.10).toLong()

            if (pointsEarned > 0) {
                transaction.update(userRef, "tukuPoints", currentPoints + pointsEarned)

                val pointHistoryRef = userRef.collection("point_history").document()
                val pointHistoryData = hashMapOf(
                    "title" to "Poin dari Pesanan #${order.orderId.take(6)}",
                    "amount" to pointsEarned,
                    "type" to "earn",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                transaction.set(pointHistoryRef, pointHistoryData)
            }
            null
        }.addOnSuccessListener {
            Toast.makeText(context, "Pesanan dikonfirmasi.", Toast.LENGTH_SHORT).show()
            val index = allOrderList.indexOfFirst { it.orderId == order.orderId }
            if (index != -1) {
                allOrderList[index].status = "Diproses"
                adapter.notifyItemChanged(index)
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreTransaction", "Gagal konfirmasi: ", e)
            Toast.makeText(context, "Gagal mengkonfirmasi pesanan: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter(
            allOrderList,
            onItemClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                startActivity(intent)
            },
            onConfirmPaymentClick = { order ->
                if (order.status == "Menunggu Pembayaran" || order.status == "Menunggu Konfirmasi") {
                    confirmPaymentAndAwardPoints(order)
                }
            },
            onAntarPesananClick = { order ->
                if (order.status == "Diproses") {
                    updateStatusAndNotify(order.orderId, "Dikirim", order.userToken, "Pesanan Dikirim", "Pesananmu sedang dalam perjalanan.", order.userEmail)
                }
            },
            onSelesaikanClick = { order ->
                 if (order.status == "Dikirim") {
                    updateStatusAndNotify(order.orderId, "Selesai", order.userToken, "Pesanan Selesai", "Terima kasih sudah memesan!", order.userEmail)
                 }
            }
        )
        rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        rvRecentOrders.adapter = adapter
    }

    private fun updateStatusAndNotify(orderId: String, newStatus: String, token: String?, title: String, body: String, targetEmail: String?) {
        db.collection("orders").document(orderId).update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Status pesanan diubah menjadi: $newStatus", Toast.LENGTH_SHORT).show()
                val index = allOrderList.indexOfFirst { it.orderId == orderId }
                if (index != -1) {
                    allOrderList[index].status = newStatus
                    adapter.notifyItemChanged(index)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengubah status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupFilterListener() {
        chipGroupFilter.check(R.id.chipAll)
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentFilterMode = when (checkedIds[0]) {
                    R.id.chipToday -> "TODAY"
                    R.id.chipYesterday -> "YESTERDAY"
                    R.id.chipWeek -> "WEEK"
                    else -> "ALL"
                }
                applyFilter()
            } else {
                currentFilterMode = "ALL"
                chipGroupFilter.check(R.id.chipAll)
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filteredList = when (currentFilterMode) {
            "TODAY" -> allOrderList.filter { isDateToday(it.orderDate) }
            "YESTERDAY" -> allOrderList.filter { isDateYesterday(it.orderDate) }
            "WEEK" -> allOrderList.filter { isDateOlderThanWeek(it.orderDate) }
            else -> allOrderList
        }

        if (filteredList.isEmpty()) {
            tvEmpty.text = if (allOrderList.isEmpty()) "Belum ada pesanan delivery." else "Tidak ada pesanan untuk filter ini."
            tvEmpty.visibility = View.VISIBLE
            rvRecentOrders.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRecentOrders.visibility = View.VISIBLE
            adapter.updateData(filteredList)
        }
    }

    private fun startListeningForOrders() {
        orderListener?.remove()
        orderListener = db.collection("orders")
            .whereEqualTo("orderType", "Delivery")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error listening for orders: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    allOrderList.clear()
                    allOrderList.addAll(snapshots.toObjects(Order::class.java))
                    applyFilter()
                }
            }
    }

    private fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        return try {
            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).parse(dateString)
        } catch (e: Exception) { null }
    }

    private fun isDateToday(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateYesterday(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val target = Calendar.getInstance().apply { time = date }
        return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateOlderThanWeek(dateString: String?): Boolean {
        val date = parseDate(dateString) ?: return false
        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        return date.before(weekAgo.time)
    }
}
