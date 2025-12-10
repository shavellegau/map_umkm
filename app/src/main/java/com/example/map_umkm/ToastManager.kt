package com.example.map_umkm

import android.content.Context
import android.widget.Toast

/**
 * Object singleton untuk mengelola Toast di seluruh aplikasi.
 * Mencegah penumpukan Toast dengan membatalkan Toast yang sedang tampil
 * sebelum menampilkan yang baru.
 */
object ToastManager {
    private var currentToast: Toast? = null

    /**
     * Menampilkan Toast yang aman dari spam.
     *
     * @param context Context dari Activity atau Fragment.
     * @param message Pesan yang akan ditampilkan.
     * @param duration Durasi Toast (default: Toast.LENGTH_SHORT).
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        // Batalkan Toast yang sedang berjalan jika ada
        currentToast?.cancel()

        // Buat dan tampilkan Toast baru, lalu simpan referensinya
        currentToast = Toast.makeText(context.applicationContext, message, duration).apply {
            show()
        }
    }
}
