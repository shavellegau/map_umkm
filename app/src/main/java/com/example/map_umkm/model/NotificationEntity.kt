package com.example.map_umkm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,

    // 🔥 CUKUP PAKAI INI SAJA (Hapus 'status') 🔥
    val type: String,

    val orderId: String?,
    val isRead: Boolean
)