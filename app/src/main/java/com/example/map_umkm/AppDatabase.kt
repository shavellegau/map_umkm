package com.example.map_umkm

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.map_umkm.data.NotificationDao
import com.example.map_umkm.model.NotificationEntity

// 🔥 PERBAIKAN 1: Naikkan version dari 1 ke 2 (atau angka lebih tinggi jika error berlanjut)
@Database(entities = [NotificationEntity::class], version = 3, exportSchema = false)
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
                )
                    // 🔥 PERBAIKAN 2: Tambahkan ini untuk reset database otomatis jika struktur berubah
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}