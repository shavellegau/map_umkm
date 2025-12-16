package com.example.map_umkm.model

import com.google.firebase.firestore.Exclude
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Cabang(
    var id: String = "",
    var nama: String = "",
    var alamat: String = "",
    var jamBuka: String = "08:00",
    var jamTutup: String = "22:00",
    var fasilitas: String = "",
    // Tambahan variabel untuk Peta & Lokasi
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) {
    // Variabel ini tidak disimpan ke Firebase, cuma untuk hitungan di aplikasi
    @get:Exclude
    var jarakHitung: Float? = null

    // Logika otomatis menentukan Buka/Tutup berdasarkan jam saat ini
    val statusBuka: String
        @Exclude get() {
            return try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val now = sdf.format(Date())
                if (now >= jamBuka && now <= jamTutup) "Buka" else "Tutup"
            } catch (e: Exception) {
                "Buka" // Default jika error parsing
            }
        }
}