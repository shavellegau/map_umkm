package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    // [FIXED] Ubah menjadi String? agar bisa menerima null dan URL internet
    val image: String? = null,
    // [FIXED] Ubah menjadi Int? agar bisa menerima null
    val price_hot: Int? = 0,
    val price_iced: Int? = 0,
    var isFavorite: Boolean = false,
    var quantity: Int = 0,
    var selectedType: String = "hot"
) : Parcelable
