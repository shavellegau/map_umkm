package com.example.map_umkm.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// [MODIFIKASI] Tambahkan 'orders' di sini
data class MenuData(
    @SerializedName("success")    val success: Boolean?,

    @SerializedName("menu")
    var menu: MutableList<MenuItem>,

    @SerializedName("orders") // <-- TAMBAHKAN INI
    var orders: MutableList<Order> // <-- TAMBAHKAN INI
)
// [PERBAIKAN] Ubah semua 'val' menjadi 'var' di sini agar nilainya bisa diedit
@Parcelize
data class MenuItem(
    @SerializedName("id")
    var id: Int,

    @SerializedName("category")
    var category: String?,

    @SerializedName("name")
    var name: String,

    @SerializedName("description")
    var description: String?,

    @SerializedName("image")
    var image: String?,

    @SerializedName("created_at")
    val createdAt: String?, // createdAt tidak perlu diedit, biarkan val

    @SerializedName("price_hot")
    var price_hot: Int?,

    @SerializedName("price_iced")
    var price_iced: Int?
) : Parcelable
