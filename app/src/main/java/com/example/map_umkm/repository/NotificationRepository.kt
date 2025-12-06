package com.example.map_umkm.repository

import android.util.Log
import com.example.map_umkm.data.NotificationDao
import com.example.map_umkm.model.NotificationEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp // 🔥 Wajib Import ini
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotificationRepository(private val notificationDao: NotificationDao) {

    // 1. Mengambil data dari Room secara Real-time (Flow)
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    // 2. 🔥 FUNGSI SINKRONISASI (Cloud -> Local) 🔥
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

                // Konversi Dokumen Firestore ke Entity Room
                val entities = documents.mapNotNull { doc ->
                    val fsTimestamp = doc.get("timestamp") as? Timestamp

                    if (fsTimestamp != null) {
                        NotificationEntity(
                            id = doc.id, // Gunakan ID Firestore agar tidak duplikat
                            title = doc.getString("title") ?: "Info",
                            body = doc.getString("body") ?: "",
                            timestamp = fsTimestamp.toDate().time, // Konversi ke Long
                            status = doc.getString("status") ?: "INFO",
                            orderId = doc.getString("orderId"),
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } else null
                }

                // Simpan ke Room di Background Thread
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Hapus cache lama (opsional, agar bersih) atau langsung insert/replace
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

    // 3. Update status 'Dibaca'
    fun updateNotificationReadStatus(notificationId: String, isRead: Boolean) {
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .document(notificationId)
            .update("isRead", isRead)
    }
}