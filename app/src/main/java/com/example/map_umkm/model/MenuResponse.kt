package com.example.map_umkm.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class MenuData(
    @SerializedName("success")    val success: Boolean?,

    @SerializedName("menu")
    var menu: MutableList<MenuItem>,

    @SerializedName("orders") 
    var orders: MutableList<Order> 
)

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
    val createdAt: String?, 

    @SerializedName("price_hot")
    var price_hot: Int?,

    @SerializedName("price_iced")
    var price_iced: Int?
) : Parcelable
