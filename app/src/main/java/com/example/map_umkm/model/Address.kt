package com.example.map_umkm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    // ID dokumen ini di Firestore, akan di-generate otomatis
    var id: String = "",

    // ID pengguna yang memiliki alamat ini
    val uid: String = "",

    // Contoh: "Rumah", "Kantor", "Apartemen"
    val label: String = "",

    // Nama penerima di alamat tsb
    val recipientName: String = "",

    // Nomor HP penerima
    val phoneNumber: String = "",

    // Detail alamat lengkap
    val fullAddress: String = "",

    // Catatan untuk kurir (opsional)
    val notes: String? = null,

    // Tandai jika ini adalah alamat utama
    var isPrimary: Boolean = false
) : Parcelable
