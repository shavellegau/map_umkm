package com.example.map_umkm.model

import java.io.Serializable
data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val imageRes: Int,
    val category: String,
    var quantity: Int = 1
): Serializable
