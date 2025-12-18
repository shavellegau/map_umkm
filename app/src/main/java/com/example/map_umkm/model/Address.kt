package com.example.map_umkm.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.IgnoredOnParcel
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

    
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable {

    @IgnoredOnParcel
    @get:Exclude
    val latLng: LatLng?
        get() = if (latitude != null && longitude != null) {
            LatLng(latitude, longitude)
        } else {
            null
        }
}