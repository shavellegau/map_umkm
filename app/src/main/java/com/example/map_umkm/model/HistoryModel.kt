package com.example.map_umkm.model

import com.google.firebase.Timestamp

data class HistoryModel(
    val title: String? = null,
    val points: Int? = 0,      
    val point: Int? = 0,       
    val type: String? = null,
    val timestamp: Timestamp? = null
)