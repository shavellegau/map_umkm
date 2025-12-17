package com.example.map_umkm.model

import com.google.firebase.Timestamp

data class HistoryModel(
    val title: String? = null,
    val points: Int? = 0,      // Menggunakan 'points' sesuai error log
    val point: Int? = 0,       // Cadangan jika di database tertulis 'point'
    val type: String? = null,
    val timestamp: Timestamp? = null
)