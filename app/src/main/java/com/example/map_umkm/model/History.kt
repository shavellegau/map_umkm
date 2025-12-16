package com.example.map_umkm.model

data class History(
    val title: String = "",
    val point: Int = 0,       // Nama field asli di database/kode lama
    val imageResId: Int = 0,  // Nama field asli
    val date: String = ""     // Tambahan umum
) {
    // Properti bantuan agar Adapter tidak error membaca 'amount' dan 'imageRes'
    val amount: Int get() = point
    val imageRes: Int get() = imageResId
}