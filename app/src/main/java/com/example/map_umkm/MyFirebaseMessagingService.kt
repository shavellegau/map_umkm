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
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }

    private val CHANNEL_ID = "fcm_default_channel"
    private val CHANNEL_NAME = "Notifikasi Tuku"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_SERVICE", "Pesan diterima dari: ${remoteMessage.from}")

        val data = remoteMessage.data

        // 1. Ambil Data Utama
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Info Tuku"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "Pesan baru."

        // 2. 🔥 KUNCI PERBAIKAN: Gunakan 'type' sebagai penentu Tab 🔥
        // Kita cek 'type' dulu. Jika kosong, cek 'status'. Jika kosong, default "INFO".
        val type = data["type"] ?: data["status"] ?: "INFO"

        val orderId = data["orderId"] // Bisa null

        // 3. Timestamp & ID
        val timestampStr = data["timestamp"]
        val timestamp = timestampStr?.toLongOrNull() ?: System.currentTimeMillis()
        val uniqueId = UUID.randomUUID().toString()

        // 4. Cek User Email
        val email = getUserEmail()
        Log.d("FCM_DEBUG", "Email user yg login: $email")

        if (email != null) {
            // Simpan tanpa parameter 'status' yang lama
            saveToFirestore(uniqueId, email, title, body, timestamp, orderId, type)
            saveToRoom(uniqueId, title, body, timestamp, orderId, type)
        }

        showNotification(title, body)
    }

    private fun saveToFirestore(
        id: String,
        email: String,
        title: String,
        body: String,
        timestamp: Long,
        orderId: String?,
        type: String // 🔥 Hanya terima type
    ) {
        val db = FirebaseFirestore.getInstance()

        val notifData = hashMapOf(
            "userEmail" to email,
            "title" to title,
            "body" to body,
            "timestamp" to timestamp,
            "type" to type, // Simpan sebagai type
            "orderId" to (orderId ?: ""),
            "isRead" to false
        )

        db.collection("notifications")
            .document(id)
            .set(notifData)
            .addOnSuccessListener {
                Log.d("FCM_FIRESTORE", "Berhasil simpan ke Firestore ID: $id")
            }
            .addOnFailureListener {
                Log.e("FCM_FIRESTORE", "Gagal simpan Firestore: ${it.message}")
            }
    }

    private fun saveToRoom(
        id: String,
        title: String,
        body: String,
        timestamp: Long,
        orderId: String?,
        type: String // 🔥 Hanya terima type
    ) {
        val dao = database.notificationDao()

        // Sesuaikan dengan Entity baru yang sudah menghapus 'status' dan pakai 'type'
        val notif = NotificationEntity(
            id = id,
            title = title,
            body = body,
            timestamp = timestamp,
            type = type,       // Masukkan ke kolom type
            orderId = orderId,
            isRead = false
        )

        scope.launch {
            try {
                dao.insert(notif)
                Log.d("FCM_ROOM", "Berhasil simpan ke Room")
            } catch (e: Exception) {
                Log.e("FCM_ROOM", "Gagal simpan Room: ${e.message}")
            }
        }
    }

    private fun getUserEmail(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null && !firebaseUser.email.isNullOrEmpty()) {
            return firebaseUser.email
        }
        return getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getString("userEmail", null)
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_ONE_SHOT

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()

        // Opsional: update ke user database jika perlu
    }
}