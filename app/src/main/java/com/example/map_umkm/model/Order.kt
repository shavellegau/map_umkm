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

    // Tetap gunakan List<Product> sesuai kode aslimu agar Adapter tidak error
    @SerializedName("items")
    val items: List<Product> = emptyList(),

    @SerializedName("totalAmount")
    val totalAmount: Double = 0.0,

    @SerializedName("orderDate")
    val orderDate: String = "",

    @SerializedName("status")
    var status: String = "Menunggu Pembayaran",

    // === UPDATE BARU: Token FCM User ===
    // Field ini wajib ada agar Admin tahu ke mana harus mengirim notifikasi
    @SerializedName("userToken")
    val userToken: String? = ""
) : Parcelable