package com.example.map_umkm.model

data class Product(
    val id: Int,
    val name: String,
    val price: String,
    val oldPrice: String? = null,
    val imageRes: Int = 0,
    val category: String // untuk filter
)