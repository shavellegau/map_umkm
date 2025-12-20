package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Voucher(
    var code: String = "",
    var title: String = "",
    var discountAmount: Double = 0.0,
    var minPurchase: Double = 0.0,
    var expiryDate: String = "",
    var description: String = "",
    var isActive: Boolean = true
) : Parcelable