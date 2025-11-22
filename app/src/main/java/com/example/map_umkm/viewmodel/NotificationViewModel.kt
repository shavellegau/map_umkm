// File: NotificationViewModel.kt
package com.example.map_umkm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.map_umkm.repository.NotificationRepository
import com.example.map_umkm.model.NotificationEntity

// ViewModel yang akan diambil oleh Fragment
class NotificationViewModel(repository: NotificationRepository) : ViewModel() {

    // Mengubah Flow menjadi LiveData agar mudah diobservasi di Fragment
    val allNotifications = repository.allNotifications.asLiveData()
}

// Factory untuk membuat ViewModel dengan Repository
class NotificationViewModelFactory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}