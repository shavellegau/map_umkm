package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Voucher(
    val code: String = "",           // ID Utama (Ex: TUKU50)
    val title: String = "",          // Ex: Diskon Kemerdekaan
    val discountAmount: Double = 0.0,// Ex: 5000.0
    val minPurchase: Double = 0.0,   // Ex: 20000.0
    val expiryDate: String = "",     // Ex: 31-12-2025
    val isActive: Boolean = true     // Status aktif/tidak
) : Parcelable