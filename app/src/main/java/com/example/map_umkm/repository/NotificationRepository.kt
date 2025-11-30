package com.example.map_umkm.repository

import com.example.map_umkm.data.NotificationDao
import com.example.map_umkm.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    // Mengambil semua notifikasi sebagai Flow (untuk update real-time)
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun insert(notification: NotificationEntity) {
        notificationDao.insert(notification)
    }
}