package com.example.map_umkm.model

data class Product(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val imageRes: Int = 0,         // pakai resource image lokal
    val category: String = "",     // kategori produk (contoh: "Makanan", "Minuman")
    var isFavorite: Boolean = false,
    var quantity: Int = 0
)
