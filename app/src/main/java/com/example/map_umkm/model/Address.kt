package com.example.map_umkm.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    // ID dokumen (disimpan sebagai string biasa, jangan pakai @DocumentId)
    var id: String? = null,

    // Label alamat (Rumah, Kantor, dll)
    var label: String? = null,

    // Nama Penerima (Mapping ke field Firestore 'recipientName')
    @get:PropertyName("recipientName")
    @set:PropertyName("recipientName")
    var recipientName: String? = null,

    // Nomor HP (Mapping ke field Firestore 'phoneNumber')
    @get:PropertyName("phoneNumber")
    @set:PropertyName("phoneNumber")
    var phoneNumber: String? = null,

    // Alamat Lengkap (Mapping ke field Firestore 'fullAddress')
    @get:PropertyName("fullAddress")
    @set:PropertyName("fullAddress")
    var fullAddress: String? = null,

    // Detail tambahan (Blok, patokan, dll)
    var details: String? = null,

    // Catatan untuk kurir
    var notes: String? = null,

    // Koordinat
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0, // <--- Pastikan ada KOMA disini

    // Penanda alamat utama
    // Menggunakan PropertyName agar Firestore tidak bingung dengan nama 'isPrimary'
    @get:PropertyName("primary")
    @set:PropertyName("primary")
    var isPrimary: Boolean = false
) : Parcelable