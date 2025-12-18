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

    private val PROJECT_ID = "map-umkm"

    fun sendNotification(target: String, title: String, body: String, orderId: String? = null, targetEmail: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken()
                if (accessToken != null) {
                    sendV1Request(accessToken, target, title, body, orderId, targetEmail)
                } else {
                    Log.e("FCM_SERVICE", "Gagal mendapatkan Access Token")
                }
            } catch (e: Exception) {
                Log.e("FCM_SERVICE", "Error Init: ${e.message}")
            }
        }
    }

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

    private suspend fun sendV1Request(
        accessToken: String,
        target: String,
        title: String,
        bodyText: String,
        orderId: String?,
        targetEmail: String?
    ) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.setRequestProperty("Content-Type", "application/json; UTF-8")
                conn.doOutput = true

                val messageContent = JSONObject()
                val notification = JSONObject()
                val data = JSONObject()
                val finalJson = JSONObject()

                val targetKey: String
                val targetValue: String

                if (target == "promo") {
                    targetKey = "topic"
                    targetValue = "promo"
                } else {
                    targetKey = "token"
                    targetValue = target
                }
                messageContent.put(targetKey, targetValue)

                val typeString: String = if (target == "promo") "PROMO" else "INFO"

                notification.put("title", title)
                notification.put("body", bodyText)

                data.put("title", title)
                data.put("body", bodyText)
                data.put("type", typeString)
                data.put("orderId", orderId ?: "")
                data.put("timestamp", System.currentTimeMillis().toString())
                data.put("targetEmail", targetEmail ?: "")

                messageContent.put("notification", notification)
                messageContent.put("data", data)

                finalJson.put("message", messageContent)

                val os = OutputStreamWriter(conn.outputStream)
                os.write(finalJson.toString())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                Log.d("FCM_SERVICE", "Status Kirim: $responseCode")

                if (responseCode >= 400) {
                    val errorStream = conn.errorStream
                    if (errorStream != null) {
                        val errorResponse = errorStream.bufferedReader().use { it.readText() }
                        Log.e("FCM_SERVICE", "GAGAL: $errorResponse")
                    }
                }

                Unit

            } catch (e: Exception) {
                Log.e("FCM_SERVICE", "Exception saat mengirim request V1: ${e.message}")
                Unit
            }
        }
    }
}