package com.example.map_umkm.model

import com.google.firebase.Timestamp

data class PointTransaction(
    val userId: String = "",
    val type: String = "", // "EARNED" atau "REDEEMED"
    val amount: Long = 0L,
    val orderId: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now()
)