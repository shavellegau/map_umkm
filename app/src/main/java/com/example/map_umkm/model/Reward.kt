// File: com/example/map_umkm/model/Reward.kt (Pastikan model Anda menggunakan nama ini)
package com.example.map_umkm.model

data class Reward(
    val title: String,
    val point: Long, // Ganti dari Int ke Long agar konsisten dengan Firestore (points)
    val imageResId: Int
)