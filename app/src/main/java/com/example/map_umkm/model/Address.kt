package com.example.map_umkm.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.IgnoredOnParcel
// [FIXED] 'import kotlinx.parcelize.Parcelize' dipindahkan ke baris baru
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    var id: String = "",
    val uid: String = "",
    val label: String = "",
    val recipientName: String = "",
    val phoneNumber: String = "",
    val fullAddress: String = "",
    val notes: String? = null,
    var isPrimary: Boolean = false,

    // Field untuk menyimpan koordinat
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable {

    // Helper untuk mengubah lat/lng menjadi objek LatLng dengan mudah
    @IgnoredOnParcel
    @get:Exclude // Agar tidak disimpan lagi oleh Firestore
    val latLng: LatLng?
        get() = if (latitude != null && longitude != null) {
            LatLng(latitude, longitude)
        } else {
            null
        }
}
