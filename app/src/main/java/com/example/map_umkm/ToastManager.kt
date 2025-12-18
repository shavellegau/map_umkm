package com.example.map_umkm

import android.content.Context
import android.widget.Toast

object ToastManager {
    private var currentToast: Toast? = null

    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context.applicationContext, message, duration).apply {
            show()
        }
    }
}
