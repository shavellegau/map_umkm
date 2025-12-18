package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Voucher(
    val code: String = "",           
    val title: String = "",          
    val discountAmount: Double = 0.0,
    val minPurchase: Double = 0.0,   
    val expiryDate: String = "",     
    val description: String = "",
    val isActive: Boolean = true     
) : Parcelable