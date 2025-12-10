package com.example.map_umkm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.map_umkm.model.NotificationEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val CHANNEL_ID = "fcm_default_channel"
    private val CHANNEL_NAME = "Notifikasi Tuku"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_SERVICE", "Pesan diterima dari: ${remoteMessage.from}")

        val data = remoteMessage.data

        val title = data["title"] ?: remoteMessage.notification?.title ?: "Info Tuku"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "Pesan baru."
        val type = data["type"] ?: data["status"] ?: "INFO"
        val orderId = data["orderId"]

        val timestampStr = data["timestamp"]
        val timestamp = timestampStr?.toLongOrNull() ?: System.currentTimeMillis()
        val uniqueId = UUID.randomUUID().toString()

        // 🔥 LOGIKA EMAIL UTAMA 🔥
        // 1. Cek payload 'targetEmail'. Jika ada, pakai itu (Akurat untuk Order).
        // 2. Jika tidak ada (Promo), baru ambil dari User yg sedang login.
        val payloadEmail = data["targetEmail"]
        val email = if (!payloadEmail.isNullOrEmpty()) payloadEmail else getUserEmail()

        Log.d("FCM_DEBUG", "Email tujuan: $email (Payload: $payloadEmail)")

        if (email != null) {
            saveToFirestore(uniqueId, email, title, body, timestamp, orderId, type)
            saveToRoom(uniqueId, title, body, timestamp, orderId, type)
        } else {
            Log.e("FCM_SERVICE", "Gagal simpan: Email tidak ditemukan.")
        }

        showNotification(title, body)
    }

    private fun saveToFirestore(id: String, email: String, title: String, body: String, timestamp: Long, orderId: String?, type: String) {
        val db = FirebaseFirestore.getInstance()
        val notifData = hashMapOf(
            "userEmail" to email, // Disimpan ke email user yang BENAR
            "title" to title,
            "body" to body,
            "timestamp" to timestamp,
            "type" to type,
            "orderId" to (orderId ?: ""),
            "isRead" to false
        )
        db.collection("notifications").document(id).set(notifData)
            .addOnSuccessListener { Log.d("FCM_FIRESTORE", "Sukses simpan ke Firestore") }
            .addOnFailureListener { Log.e("FCM_FIRESTORE", "Gagal simpan: ${it.message}") }
    }

    private fun saveToRoom(id: String, title: String, body: String, timestamp: Long, orderId: String?, type: String) {
        val dao = database.notificationDao()
        val notif = NotificationEntity(id, title, body, timestamp, type, orderId, false)
        scope.launch {
            try { dao.insert(notif) }
            catch (e: Exception) { Log.e("FCM_ROOM", "Gagal simpan Room: ${e.message}") }
        }
    }

    private fun getUserEmail(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null && !firebaseUser.email.isNullOrEmpty()) return firebaseUser.email
        return getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE).getString("userEmail", null)
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_ONE_SHOT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}