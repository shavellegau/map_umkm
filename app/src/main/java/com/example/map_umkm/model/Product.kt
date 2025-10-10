package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val category: String,
    val name: String,
    val description: String?,
    val image: String?,
    val price_hot: Int,
    val price_iced: Int?,
    var quantity: Int = 1,
    var selectedType: String = "hot"
) : Parcelable // Corrected: Use Parcelable instead of Serializable