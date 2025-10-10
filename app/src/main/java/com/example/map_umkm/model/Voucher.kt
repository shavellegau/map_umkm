package com.example.map_umkm.model

data class Voucher(
    val id: String,
    val title: String,
    val code: String,
    val description: String,
    val expiryDate: String,
    val isUsed: Boolean
)