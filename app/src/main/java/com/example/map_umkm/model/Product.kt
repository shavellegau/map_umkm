package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val description: String?,
    val image: String?,
    val price_hot: Int,
    val price_iced: Int?,
    var isFavorite: Boolean = false, // From the second data class
    var quantity: Int = 0,
    var selectedType: String = "hot"
) : Parcelable
