package com.example.map_umkm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: Double
)
