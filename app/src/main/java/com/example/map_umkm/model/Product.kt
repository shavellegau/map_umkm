package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val image: String = "",
    val price_hot: Int = 0,
    val price_iced: Int = 0,
    var isFavorite: Boolean = false,
    var quantity: Int = 0,
    var selectedType: String = "hot"
) : Parcelable
