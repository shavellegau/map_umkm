package com.example.map_umkm.model

data class Cabang(
    var id: String = "",
    val nama: String = "",
    val alamat: String = "",
    val jamBuka: String = "",
    val statusBuka: String = "",
    val detail: String = "",

    // 🔥 WAJIB DITAMBAHKAN UNTUK FITUR MAPS 🔥
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)