package com.example.map_umkm.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val body: String,

    // PASTIKAN INI ADA TANDA TANYA (?) AGAR BOLEH NULL
    val orderId: String? = null,

    val status: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
)