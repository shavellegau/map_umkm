package com.example.map_umkm.model

import com.google.firebase.Timestamp

data class History(
    val title: String = "",
    val point: Int = 0,
    val type: String = "redeem", // "earn" atau "redeem"
    val timestamp: Timestamp? = null,
    val imageResId: Int = 0 // Ini default 0 karena Firestore tidak menyimpan ID gambar Android
)