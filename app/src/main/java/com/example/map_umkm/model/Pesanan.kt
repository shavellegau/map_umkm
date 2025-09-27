package com.example.map_umkm.model

data class Pesanan(
    val nama: String,
    val detail: String,
    val harga: Int,
    val imageRes: Int // pakai drawable lokal, bisa nanti diganti URL
)
