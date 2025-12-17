package com.example.map_umkm.model

data class MembershipReward(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val discount: String = "", // e.g., "50% Maks. 20k"
    val min_tier: Int = 0 // 0=Bronze, 1=Silver, 2=Gold, 3=Platinum, 4=Diamond
)