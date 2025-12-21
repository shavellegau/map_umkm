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

class AdminOrdersFragment : Fragment() {

    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter
    private lateinit var chipGroupFilter: ChipGroup

    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalUsers: TextView

    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null

    private var allOrderList: List<Order> = emptyList()
    private var currentFilterMode = "ALL"

    private var statsListener1: ListenerRegistration? = null
    private var statsListener2: ListenerRegistration? = null
    private var statsListener3: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)
        fcmService = FCMService(requireContext())

        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter)
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders)
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts)
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)

        setupRecyclerView()
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

    private fun completeOrderAndAwardPoints(order: Order) {
        if (order.userId.isEmpty()) {
            Toast.makeText(context, "Error: User ID kosong.", Toast.LENGTH_LONG).show()
            return
        }

        val orderRef = db.collection("orders").document(order.orderId)
        val userRef = db.collection("users").document(order.userId)

        val subtotal = order.items.sumOf { item ->
            val price = (if (item.selectedType == "iced") item.price_iced else item.price_hot) ?: 0
            price * item.quantity.toDouble()
        }
        val pointsEarned = (subtotal * 0.10).toInt()

        db.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val currentPoints = userSnapshot.getLong("tukuPoints") ?: 0

            transaction.update(orderRef, "status", "Selesai")

            if (pointsEarned > 0) {
                val newTotalPoints = currentPoints + pointsEarned
                transaction.update(userRef, "tukuPoints", newTotalPoints)

                val pointHistoryRef = userRef.collection("point_history").document()
                val pointHistoryData = hashMapOf(
                    "title" to "Poin dari Pesanan",
                    "amount" to pointsEarned.toLong(),
                    "type" to "earn",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                transaction.set(pointHistoryRef, pointHistoryData)
            }
            null
        }.addOnSuccessListener {
            Toast.makeText(context, "Pesanan Selesai. Poin: $pointsEarned", Toast.LENGTH_SHORT).show()

            sendNotificationToLatestToken(
                order.userId,
                "Pesanan Selesai",
                "Terima kasih! Kamu mendapatkan $pointsEarned poin.",
                order.orderId,
                order.userEmail
            )

        }.addOnFailureListener { e ->
            Toast.makeText(context, "Gagal menyelesaikan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStatusAndNotify(
        orderId: String, newStatus: String, userId: String, title: String, body: String, targetEmail: String?
    ) {
        db.collection("orders").document(orderId).update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Status -> $newStatus", Toast.LENGTH_SHORT).show()
                sendNotificationToLatestToken(userId, title, body, orderId, targetEmail)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotificationToLatestToken(userId: String, title: String, body: String, orderId: String, email: String?) {
        Log.d("FCM_SERVICE", "Mencoba ambil token untuk UserId: $userId")

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val freshToken = document.getString("fcmToken")

                    if (!freshToken.isNullOrEmpty()) {
                        Log.d("FCM_SERVICE", "Token ditemukan: $freshToken")
                        fcmService.sendNotification(freshToken, title, body, orderId, email)
                    } else {
                        Log.e("FCM_SERVICE", "GAGAL: User $userId tidak memiliki token")
                    }

                    saveNotificationToDatabase(userId, title, body, orderId)
                } else {
                    Log.e("FCM_SERVICE", "User ID tidak ditemukan di database")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM_SERVICE", "Gagal mengambil data user: ${e.message}")
            }
    }

    private fun saveNotificationToDatabase(userId: String, title: String, body: String, orderId: String) {
        val notifData = hashMapOf(
            "title" to title,
            "body" to body,
            "orderId" to orderId,
            "isRead" to false,
            "timestamp" to FieldValue.serverTimestamp(),
            "type" to "INFO"
        )

        db.collection("users").document(userId)
            .collection("notifications")
            .add(notifData)
            .addOnSuccessListener {
                Log.d("FCM_SERVICE", "Notifikasi tersimpan di DB")
            }
            .addOnFailureListener {
                Log.e("FCM_SERVICE", "Gagal simpan DB")
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
                updateStatusAndNotify(order.orderId, "Diproses", order.userId, "Pesanan Diproses", "Pembayaran sedang dicek Admin.", order.userEmail)
            },
            onAntarPesananClick = { order ->
                updateStatusAndNotify(order.orderId, "Dikirim", order.userId, "Pesanan Dikirim", "Pesananmu sedang dalam perjalanan.", order.userEmail)
            },
            onSelesaikanClick = { order ->
                completeOrderAndAwardPoints(order)
            }
        )
        rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        rvRecentOrders.adapter = adapter
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
            tvEmpty.text = if (allOrderList.isEmpty()) "Belum ada pesanan." else "Tidak ada pesanan untuk filter ini."
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
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    allOrderList = snapshots.toObjects(Order::class.java)
                    applyFilter()
                }
            }
    }

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
}