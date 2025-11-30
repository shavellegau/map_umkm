package com.example.map_umkm.model

data class Cabang(
    var id: String = "", // ID unik, disarankan sama dengan ID dokumen Firestore
    val nama: String = "",
    val alamat: String = "",
    val jamBuka: String = "",
    val statusBuka: String = "",
    val detail: String = ""
)