package com.example.map_umkm.model

import com.google.gson.annotations.SerializedName

// Class ini sudah benar, tidak perlu diubah
data class MenuData(
    @SerializedName("success")
    val success: Boolean?,

    @SerializedName("menu")
    var menu: MutableList<MenuItem>
)

// [PERBAIKAN] Ubah semua 'val' menjadi 'var' di sini agar nilainya bisa diedit
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
)
