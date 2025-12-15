package com.example.map_umkm.model

data class HistoryModel(
    var title: String = "",
    var points: Int = 0,
    var type: String = "", // earn atau redeem
    var timestamp: Long = 0
)
