package com.example.map_umkm.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    @SerializedName("orderId")
    val orderId: String = "", // Tambahkan nilai default untuk Parcelize

    @SerializedName("userEmail")
    val userEmail: String = "",

    @SerializedName("userName")
    val userName: String = "",

    @SerializedName("items")
    val items: List<Product> = emptyList(), // Gunakan List<Product> dari proyek Anda

    @SerializedName("totalAmount")
    val totalAmount: Double = 0.0,

    @SerializedName("orderDate")
    val orderDate: String = "",

    @SerializedName("status")
    var status: String = "Menunggu Pembayaran"
) : Parcelable