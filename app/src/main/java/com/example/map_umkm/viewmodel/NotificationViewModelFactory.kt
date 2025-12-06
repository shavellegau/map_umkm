package com.example.map_umkm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.map_umkm.repository.NotificationRepository

class NotificationViewModelFactory(private val repository: NotificationRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Mengecek apakah ViewModel yang diminta adalah NotificationViewModel
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Membuat instance NotificationViewModel dengan repository
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}