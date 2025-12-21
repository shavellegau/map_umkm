package com.example.map_umkm.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("userId")
    val userId: String = "", 

    @SerializedName("userEmail")
    val userEmail: String = "",

    @SerializedName("userName")
    var userName: String = "",

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

    @SerializedName("deliveryAddress")
    val deliveryAddress: Address? = null,

    @SerializedName("orderType")
    val orderType: String = "Take Away",

    // Detailed Order Summary
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val shippingCost: Double = 0.0,
    val voucherDiscount: Double = 0.0,
    val pointsUsed: Double = 0.0,
    val pointsEarned: Long = 0,
    val expEarned: Long = 0

) : Parcelable
