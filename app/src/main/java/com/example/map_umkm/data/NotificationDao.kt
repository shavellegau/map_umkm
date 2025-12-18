
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

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :notificationId")
    suspend fun updateIsRead(notificationId: String, isRead: Boolean)
    
    @Query("SELECT * FROM notifications WHERE type != 'PROMO' ORDER BY timestamp DESC")
    fun getInfoNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE type = 'PROMO' ORDER BY timestamp DESC")
    fun getPromoNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("DELETE FROM notifications WHERE type = 'PROMO'")
    suspend fun deleteAllPromos()

    @Query("DELETE FROM notifications WHERE type != 'PROMO'")
    suspend fun deleteAllInfo()
}