// File: com/example/map_umkm/data/NotificationDao.kt
package com.example.map_umkm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.map_umkm.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // Simpan satu notifikasi (dipakai MyFirebaseMessagingService)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    // 🔥 FIX ERROR: insertAll (dipakai Repository saat sync)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    // 🔥 FIX ERROR: getAllNotifications (dipakai Repository)
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    // 🔥 FIX ERROR: deleteAll (dipakai Repository saat reset data)
    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    // Update status baca
    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :notificationId")
    suspend fun updateIsRead(notificationId: String, isRead: Boolean)
}