package com.example.map_umkm.model

data class Reward(
    val id: String = "",
    val title: String = "",        // Contoh: "Diskon Poin"
    val point: Int = 0,            // Contoh: 500
    val discountAmount: Int = 0,   // Contoh: 10000 (Rupiah)
    val imageResId: Int = 0
)