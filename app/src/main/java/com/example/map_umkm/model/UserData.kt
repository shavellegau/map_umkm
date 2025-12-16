package com.example.map_umkm.model

import com.google.firebase.Timestamp

data class UserData(
    val currentPoints: Int = 0,
    val currentXp: Int = 0,
    val lastTransactionDate: Timestamp? = null
)