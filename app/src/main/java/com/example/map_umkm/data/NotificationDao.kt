package com.example.map_umkm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.map_umkm.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /**
     * Menyisipkan notifikasi baru ke database.
     * Menggunakan OnConflictStrategy.REPLACE jika ID notifikasi sudah ada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    /**
     * Mengambil semua notifikasi dari tabel, diurutkan dari yang terbaru (DESCENDING).
     */
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    // Anda dapat menambahkan fungsi lain seperti:

    // @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    // suspend fun markAsRead(notificationId: Int)
}