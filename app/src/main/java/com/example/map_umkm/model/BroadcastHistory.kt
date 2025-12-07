package com.example.map_umkm.model

import com.google.firebase.Timestamp

data class BroadcastHistory(
    var id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Timestamp? = null
)