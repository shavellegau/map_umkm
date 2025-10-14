package com.example.map_umkm.repository

import androidx.lifecycle.LiveData
import com.example.map_umkm.data.FavoriteDao
import com.example.map_umkm.model.Favorite

class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    val allFavorites: LiveData<List<Favorite>> = favoriteDao.getAllFavorites()

    suspend fun insert(favorite: Favorite) {
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun delete(favorite: Favorite) {
        favoriteDao.deleteFavorite(favorite)
    }

    suspend fun clear() {
        favoriteDao.clearAll()
    }
}
