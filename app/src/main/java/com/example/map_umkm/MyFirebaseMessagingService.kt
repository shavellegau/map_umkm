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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Gunakan Dispatchers.IO untuk operasi database
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Lazy init database
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }

    private val CHANNEL_ID = "fcm_default_channel"
    private val CHANNEL_NAME = "Notifikasi Tuku"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_SERVICE", "Pesan diterima: ${remoteMessage.from}")

        // PRIORITAS 1: Cek Payload DATA (Yang berisi detail Order/Promo)
        // Kita memprioritaskan ini karena mengandung info lengkap untuk Database
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }
        // PRIORITAS 2: Jika tidak ada Data, baru cek Payload Notification standar
        else if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title ?: "Info"
            val body = remoteMessage.notification?.body ?: ""
            showNotification(title, body)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "Pembaruan Aplikasi"
        val body = data["body"] ?: "Informasi baru tersedia."
        val status = data["status"] ?: "INFO"

        // Handle OrderID: Jika Promo, orderId mungkin null atau string kosong
        // Kita pastikan aman agar tidak error di Database
        val orderId = data["orderId"] // Bisa null

        val timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()

        // 1. Simpan ke Room Database
        val notifEntity = NotificationEntity(
            title = title,
            body = body,
            timestamp = timestamp,
            status = status,
            orderId = orderId, // Room Entity harus mengizinkan ini Nullable (String?)
            isRead = false
        )

        scope.launch {
            try {
                database.notificationDao().insert(notifEntity)
                Log.d("FCM_ROOM", "Notif berhasil disimpan ke DB Lokal")
            } catch (e: Exception) {
                Log.e("FCM_ROOM", "Gagal simpan ke DB: ${e.message}")
            }
        }

        // 2. Tampilkan Notifikasi Bunyi Ting!
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM_SERVICE", "Token Refresh: $token")
        // Simpan ke SharedPrefs agar MainActivity bisa ambil nanti
        getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()

        // Opsional: Simpan ke Firestore (jika pakai)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
        }
    }

    private fun showNotification(title: String, message: String) {
        // Intent agar saat diklik membuka MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Pastikan ganti dengan icon transparan jika punya
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Agar teks panjang terbaca
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Gunakan Timestamp unik sebagai ID agar notifikasi menumpuk (tidak saling timpa)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}