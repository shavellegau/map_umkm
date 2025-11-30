package com.example.map_umkm

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FCMService(private val context: Context) {

    private val PROJECT_ID = "map-umkm"   // Pastikan sama dengan Firebase Project ID

    fun sendNotification(target: String, title: String, body: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken()

                if (accessToken != null) {
                    sendV1Request(accessToken, target, title, body)
                } else {
                    Log.e("FCM_SERVICE", "Gagal mendapatkan Access Token. Pastikan service_account.json ada di folder assets.")
                }

            } catch (e: Exception) {
                Log.e("FCM_SERVICE", "Error saat mengirim notifikasi: ${e.message}")
            }
        }
    }

    // [FIXED] Mengambil access token dari file JSON di folder 'assets'
    private fun getAccessToken(): String? {
        return try {
            // Buka file dari folder assets, bukan res/raw
            val inputStream = context.assets.open("service_account.json")

            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue

        } catch (e: Exception) {
            Log.e("FCM_AUTH", "Error membaca service_account.json dari assets: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private suspend fun sendV1Request(
        accessToken: String,
        target: String,
        title: String,
        bodyText: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.setRequestProperty("Content-Type", "application/json; UTF-8")
                conn.doOutput = true

                val messageJson = JSONObject()
                val messageContent = JSONObject()

                if (target == "promo") {
                    messageContent.put("topic", "promo")
                } else {
                    messageContent.put("token", target)
                }

                val notification = JSONObject()
                notification.put("title", title)
                notification.put("body", bodyText)

                val data = JSONObject()
                data.put("title", title)
                data.put("body", bodyText)
                data.put("status", "UPDATE")
                data.put("timestamp", System.currentTimeMillis().toString())

                messageContent.put("notification", notification)
                messageContent.put("data", data)

                messageJson.put("message", messageContent)

                val os = OutputStreamWriter(conn.outputStream)
                os.write(messageJson.toString())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                Log.d("FCM_SERVICE", "Status Kirim Notifikasi: $responseCode")
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = conn.errorStream?.bufferedReader().use { it?.readText() }
                    Log.e("FCM_SERVICE", "Gagal mengirim notifikasi: $errorStream")
                }

            } catch (e: Exception) {
                Log.e("FCM_SEND", "Error dalam sendV1Request: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
