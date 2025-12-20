package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val id: String = "",
    val label: String = "",
    val fullAddress: String = "",
    val recipientName: String = "",
    val phoneNumber: String = "",

    // --- FIELD YANG SEBELUMNYA HILANG (TAMBAHKAN INI) ---
    val details: String = "",
    val notes: String = "",
    // ----------------------------------------------------

    // Tetap gunakan Double? (Nullable) untuk mencegah crash
    val latitude: Double? = null,
    val longitude: Double? = null,

    val isPrimary: Boolean = false
) : Parcelable