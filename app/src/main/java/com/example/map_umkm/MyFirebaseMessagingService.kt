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

        var title = remoteMessage.data["title"] ?: "Info Tuku"
        var body = remoteMessage.data["body"] ?: "Ada pesan baru untukmu."
        var status = remoteMessage.data["status"] ?: "INFO"
        var orderId = remoteMessage.data["orderId"]
        var timestamp = remoteMessage.data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()

        saveNotificationToDatabase(title, body, timestamp, status, orderId)
        showNotification(title, body)
    }

    private fun saveNotificationToDatabase(
        title: String,
        body: String,
        timestamp: Long,
        status: String,
        orderId: String?
    ) {
        val id = UUID.randomUUID().toString()
        val userEmail = getUserEmail()

        if (userEmail == null) {
            Log.e("FCM_ERROR", "Email user NULL → notifikasi tidak bisa disimpan ke Firestore")
            return
        }

        Log.d("FCM_DEBUG", "Simpan notifikasi untuk user: $userEmail")

        // SAVE ROOM
        val notif = NotificationEntity(
            id = id,
            title = title,
            body = body,
            timestamp = timestamp,
            status = status,
            orderId = orderId,
            isRead = false
        )

        scope.launch {
            try {
                database.notificationDao().insert(notif)
                Log.d("FCM_ROOM", "Room ✔ Berhasil simpan")
            } catch (e: Exception) {
                Log.e("FCM_ROOM", "Room ✖ Error: ${e.message}")
            }
        }

        // SAVE FIRESTORE
        val data = mapOf(
            "id" to id,
            "title" to title,
            "body" to body,
            "timestamp" to timestamp,
            "status" to status,
            "orderId" to (orderId ?: ""),
            "isRead" to false,
            "userEmail" to userEmail           // <= FIX UTAMA
        )

        scope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .document(id)
                    .set(data, SetOptions.merge())

                Log.d("FCM_FIRESTORE", "Firestore ✔ Berhasil simpan untuk $userEmail")
            } catch (e: Exception) {
                Log.e("FCM_FIRESTORE", "Firestore ✖ Error: ${e.message}")
            }
        }
    }

    private fun getUserEmail(): String? {
        val firebase = FirebaseAuth.getInstance().currentUser
        if (firebase?.email != null) return firebase.email

        val prefs = applicationContext.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        return prefs.getString("userEmail", null)
    }

    override fun onNewToken(token: String) {
        getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
