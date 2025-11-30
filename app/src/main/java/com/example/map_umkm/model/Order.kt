package com.example.map_umkm.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("userEmail")
    val userEmail: String = "",

    @SerializedName("userName")
    val userName: String = "",

    @SerializedName("items")
    val items: List<Product> = emptyList(),

    @SerializedName("totalAmount")
    val totalAmount: Double = 0.0,

    @SerializedName("orderDate")
    val orderDate: String = "",

    @SerializedName("status")
    var status: String = "Menunggu Pembayaran",

    @SerializedName("userToken")
    val userToken: String? = "",

    // ===================================
    //  PENAMBAHAN UNTUK FITUR DELIVERY
    // ===================================
    @SerializedName("isDelivery")
    val isDelivery: Boolean = false, // Tandai apakah ini pesanan delivery

    @SerializedName("deliveryAddress")
    val deliveryAddress: String? = null, // Alamat pengiriman

    @SerializedName("shippingCost")
    val shippingCost: Double = 0.0 // Ongkos kirim
    // ===================================

) : Parcelable
