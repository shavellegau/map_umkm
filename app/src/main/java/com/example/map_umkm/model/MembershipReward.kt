package com.example.map_umkm.model

data class MembershipReward(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val discount: String = "", 
    val min_tier: Int = 0 
)