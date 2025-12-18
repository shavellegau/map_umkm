package com.example.map_umkm.repository

import android.util.Log
import com.example.map_umkm.data.NotificationDao
import com.example.map_umkm.model.NotificationEntity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationRepository(private val notificationDao: NotificationDao) {

    val infoNotifications = notificationDao.getInfoNotifications()
    val promoNotifications = notificationDao.getPromoNotifications()
    val allNotifications = notificationDao.getAllNotifications()

    private val db = FirebaseFirestore.getInstance()
    
    fun syncPersonalOrders(userEmail: String) {
        db.collection("notifications")
            .whereEqualTo("userEmail", userEmail)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->

                val list = documents.mapNotNull { doc ->
                    val ts = doc.get("timestamp") as? Timestamp ?: return@mapNotNull null
                    NotificationEntity(
                        id = doc.id,
                        title = doc.getString("title") ?: "Info",
                        body = doc.getString("body") ?: "",
                        timestamp = ts.toDate().time,
                        type = doc.getString("type") ?: "INFO",
                        orderId = doc.getString("orderId"),
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        notificationDao.insertAll(list)
                    } catch (e: Exception) {
                        Log.e("REPO", "Error sync info: ${e.message}")
                    }
                }
            }
    }

    fun syncPromosFromAdmin() {
        db.collection("broadcast_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->

                val list = documents.mapNotNull { doc ->
                    val ts = doc.get("timestamp") as? Timestamp ?: return@mapNotNull null
                    NotificationEntity(
                        id = doc.id,
                        title = doc.getString("title") ?: "Promo",
                        body = doc.getString("body") ?: "",
                        timestamp = ts.toDate().time,
                        type = "PROMO",
                        orderId = null,
                        isRead = false
                    )
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        notificationDao.deleteAllPromos()
                        notificationDao.insertAll(list)
                    } catch (e: Exception) {
                        Log.e("REPO", "Error sync promo: ${e.message}")
                    }
                }
            }
    }

    fun updateNotificationReadStatus(id: String, isRead: Boolean) {
        db.collection("notifications").document(id).update("isRead", isRead)
    }
}
