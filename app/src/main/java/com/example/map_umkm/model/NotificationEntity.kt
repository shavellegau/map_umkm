// File: com/example/map_umkm/model/NotificationEntity.kt
package com.example.map_umkm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String, // 🔥 HARUS STRING agar cocok dengan ID dari Firestore/UUID
    val title: String,
    val body: String,
    val timestamp: Long,
    val status: String,
    val orderId: String?,
    val isRead: Boolean
)