package com.example.map_umkm

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.map_umkm.data.NotificationDao // Perbaiki impor sesuai struktur Anda
import com.example.map_umkm.model.NotificationEntity // Perbaiki impor sesuai struktur Anda

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Abstract method untuk mengakses DAO
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umkm_app_database" // Nama file database lokal
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}