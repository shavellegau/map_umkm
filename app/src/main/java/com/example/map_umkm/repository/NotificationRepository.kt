// File: com/example/map_umkm/repository/NotificationRepository.kt
package com.example.map_umkm.repository

import android.util.Log
import com.example.map_umkm.data.NotificationDao
import com.example.map_umkm.model.NotificationEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp // ✅ Import yang benar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotificationRepository(private val notificationDao: NotificationDao) {

    // 🔥 1. Sumber Data Terpisah untuk ViewModel 🔥
    val infoNotifications: Flow<List<NotificationEntity>> = notificationDao.getInfoNotifications()
    val promoNotifications: Flow<List<NotificationEntity>> = notificationDao.getPromoNotifications()

    // (Opsional) Jika masih butuh list gabungan
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    // 2. Fungsi Sinkronisasi (Tetap ambil SEMUA dari Firestore)
    fun syncCloudToLocal(userEmail: String) {
        val db = FirebaseFirestore.getInstance()

        Log.d("REPO", "Mulai sync notifikasi untuk: $userEmail")

        db.collection("notifications")
            .whereEqualTo("userEmail", userEmail)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("REPO", "Firestore kosong untuk user ini.")
                    return@addOnSuccessListener
                }

                val entities = documents.mapNotNull { doc ->
                    val fsTimestamp = doc.get("timestamp") as? Timestamp

                    if (fsTimestamp != null) {
                        NotificationEntity(
                            id = doc.id,
                            title = doc.getString("title") ?: "Info",
                            body = doc.getString("body") ?: "",
                            timestamp = fsTimestamp.toDate().time,
                            // Pastikan status di Firestore benar ('PROMO' atau 'INFO'/'UPDATE')
                            type = doc.getString("type") ?: doc.getString("status") ?: "INFO",
                            orderId = doc.getString("orderId"),
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } else null
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        notificationDao.deleteAll()
                        notificationDao.insertAll(entities)
                        Log.d("REPO", "Berhasil sync ${entities.size} notifikasi ke Room")
                    } catch (e: Exception) {
                        Log.e("REPO", "Gagal insert ke Room: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("REPO", "Gagal ambil dari Firestore: ${e.message}")
            }
    }

    // 3. Update Status Baca
    fun updateNotificationReadStatus(notificationId: String, isRead: Boolean) {
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .document(notificationId)
            .update("isRead", isRead)
    }
}