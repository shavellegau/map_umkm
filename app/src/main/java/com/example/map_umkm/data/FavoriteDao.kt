package com.example.map_umkm.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.map_umkm.model.Favorite

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): LiveData<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}
