// File: com/example/map_umkm/viewmodel/NotificationViewModel.kt
package com.example.map_umkm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.map_umkm.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    // 🔥 LiveData Terpisah untuk Tab UI 🔥
    val infoList = repository.infoNotifications.asLiveData()
    val promoList = repository.promoNotifications.asLiveData()

    // (Opsional) Data gabungan
    val allNotifications = repository.allNotifications.asLiveData()

    // Fungsi Sync
    fun syncCloud(userEmail: String) {
        viewModelScope.launch {
            repository.syncCloudToLocal(userEmail)
        }
    }

    // Fungsi Mark as Read
    fun markAsRead(id: String) {
        repository.updateNotificationReadStatus(id, true)
    }
}