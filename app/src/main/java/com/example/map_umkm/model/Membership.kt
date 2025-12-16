package com.example.map_umkm.model

data class Membership(
    val userId: String = "",
    val levelName: String = "Jiwa",
    val currentXp: Int = 0,
    val targetXp: Int = 100,
    val currentPoints: Int = 0
)