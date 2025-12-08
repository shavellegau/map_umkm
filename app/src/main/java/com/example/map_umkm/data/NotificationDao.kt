package com.example.map_umkm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.map_umkm.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :notificationId")
    suspend fun updateIsRead(notificationId: String, isRead: Boolean)

    // 🔥 PERHATIKAN: Ganti 'status' menjadi 'type' di Query SQL ini 🔥

    // 1. Tab Info Pesanan (Ambil yang BUKAN PROMO)
    @Query("SELECT * FROM notifications WHERE type != 'PROMO' ORDER BY timestamp DESC")
    fun getInfoNotifications(): Flow<List<NotificationEntity>>

    // 2. Tab Promo (Ambil KHUSUS PROMO)
    @Query("SELECT * FROM notifications WHERE type = 'PROMO' ORDER BY timestamp DESC")
    fun getPromoNotifications(): Flow<List<NotificationEntity>>

    // 3. Ambil Semua
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>
}