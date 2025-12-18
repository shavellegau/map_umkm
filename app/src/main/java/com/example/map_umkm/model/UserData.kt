package com.example.map_umkm.model

import com.google.firebase.Timestamp

data class UserData(
    val uid: String = "",
    val name: String = "",         
    val email: String = "",
    val points: Int = 0,           
    val currentPoints: Int = 0,    
    val currentXp: Int = 0,
    val lastTransactionDate: Timestamp? = null
)