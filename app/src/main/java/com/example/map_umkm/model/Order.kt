package com.example.map_umkm.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Serializable memungkinkan objek ini dikirim antar activity/fragment
data class Order(
    @SerializedName("orderId")
    val orderId: String,

    @SerializedName("userEmail")
    val userEmail: String,

    @SerializedName("userName")
    val userName: String,

    @SerializedName("items")
    // [FIXED] Tipe data diubah ke model Product dari proyek Anda
    val items: List<Product>,

    @SerializedName("totalAmount")
    val totalAmount: Double,

    @SerializedName("orderDate")
    val orderDate: String,

    @SerializedName("status")
    var status: String // Status: "Menunggu Konfirmasi", "Diproses", "Selesai"

    // [FIXED] Semua properti duplikat dan tidak perlu dari model dummy sudah dihapus
) : Serializable
