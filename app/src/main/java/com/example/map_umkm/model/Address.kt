package com.example.map_umkm.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    var id: String? = null,

    var label: String? = null,

    @get:PropertyName("recipientName")
    @set:PropertyName("recipientName")
    var recipientName: String? = null,

    @get:PropertyName("phoneNumber")
    @set:PropertyName("phoneNumber")
    var phoneNumber: String? = null,

    @get:PropertyName("fullAddress")
    @set:PropertyName("fullAddress")
    var fullAddress: String? = null,

    var details: String? = null,

    var notes: String? = null,

    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,

    @get:PropertyName("primary")
    @set:PropertyName("primary")
    var isPrimary: Boolean = false
) : Parcelable