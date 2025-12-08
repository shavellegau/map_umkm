package com.example.map_umkm   // <--- pastikan sama seperti project kamu

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

    // ===============================
    //  PUBLIC → ADMIN memanggil ini
    // ===============================
    fun sendNotification(target: String, title: String, body: String) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken()

                if (accessToken != null) {
                    // target bisa token ATAU "promo"
                    sendV1Request(accessToken, target, title, body)
                } else {
                    Log.e("FCM_SERVICE", "Gagal baca service_account.json")
                }

            } catch (e: Exception) {
                Log.e("FCM_SERVICE", "Error: ${e.message}")
            }
        }
    }


    // ===============================
    //  MENGAMBIL ACCESS TOKEN GOOGLE
    // ===============================
    private fun getAccessToken(): String? {
        return try {

            val inputStream = context.resources.openRawResource(R.raw.service_account)
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // ======================================================
    //  FCM HTTP v1 — SUDAH ADA LOGIKA TOPIK VS PERSONAL TOKEN
    // ======================================================
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

                // ... (Bagian pembuatan JSON Anda tetap sama) ...
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
                // Tambahkan orderId jika ada, atau string kosong jika broadcast
                data.put("orderId", "")
                data.put("timestamp", System.currentTimeMillis().toString())

                messageContent.put("notification", notification)
                messageContent.put("data", data)
                messageJson.put("message", messageContent)

                // Tulis Output
                val os = OutputStreamWriter(conn.outputStream)
                os.write(messageJson.toString())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                Log.d("FCM_SERVICE", "Status Kirim: $responseCode")

                // 🔥 TAMBAHAN PENTING: BACA ERROR JIKA GAGAL 🔥
                if (responseCode >= 400) {
                    val errorStream = conn.errorStream
                    if (errorStream != null) {
                        val errorResponse = errorStream.bufferedReader().use { it.readText() }
                        Log.e("FCM_SERVICE", "GAGAL KIRIM! Detail Error: $errorResponse")
                    }
                } else {
                    Log.d("FCM_SERVICE", "Berhasil terkirim ke: $target")
                }

            } catch (e: Exception) {
                Log.e("FCM_SERVICE", "Exception saat kirim: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
