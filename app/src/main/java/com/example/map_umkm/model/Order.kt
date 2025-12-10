package com.example.map_umkm.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    @SerializedName("orderId")
    val orderId: String = "",

    // --- TAMBAHKAN BARIS INI ---
    @SerializedName("userId")
    val userId: String = "", // WAJIB ADA untuk referensi ke dokumen user

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

    @SerializedName("deliveryAddress")
    val deliveryAddress: Address? = null,

    @SerializedName("orderType")
    val orderType: String = "Take Away"

) : Parcelable
