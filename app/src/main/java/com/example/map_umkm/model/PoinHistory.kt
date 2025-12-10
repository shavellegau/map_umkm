// File: com/example/map_umkm/model/PoinHistory.kt
package com.example.map_umkm.model

data class PoinHistory(
    val title: String,
    val amount: String, // String karena bisa berisi "+500" atau "-2000"
    val date: String
)