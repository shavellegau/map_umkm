package com.example.map_umkm.model

import com.google.firebase.Timestamp

// Kita satukan semua kemungkinan field agar tidak error lagi
data class UserData(
    val uid: String = "",
    val name: String = "",         // Tambahan untuk fix error 'name'
    val email: String = "",
    val points: Int = 0,           // Tambahan untuk fix error 'points'
    val currentPoints: Int = 0,    // Field lama (cadangan)
    val currentXp: Int = 0,
    val lastTransactionDate: Timestamp? = null
)