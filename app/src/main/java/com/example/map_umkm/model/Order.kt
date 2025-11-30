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

    @SerializedName("discount")
    // GANTI TIPE DATA DARI Int ke Long? agar kompatibel dengan data server dan null safety
    val discountAmount: Long? = 0L,

    // === UPDATE BARU: Token FCM User ===
    @SerializedName("userToken")
    val userToken: String = ""
) : Parcelable