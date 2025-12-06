package com.example.map_umkm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.map_umkm.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    // Data Live dari Room untuk ditampilkan di UI
    val allNotifications = repository.allNotifications.asLiveData()

    // 🔥 Panggil fungsi sinkronisasi di Repository 🔥
    fun syncCloud(userEmail: String) {
        viewModelScope.launch {
            repository.syncCloudToLocal(userEmail)
        }
    }

    fun markAsRead(id: String) {
        repository.updateNotificationReadStatus(id, true)
    }
}
