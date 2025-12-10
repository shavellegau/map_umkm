package com.example.map_umkm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.map_umkm.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    val infoList = repository.infoNotifications.asLiveData()
    val promoList = repository.promoNotifications.asLiveData()
    val allNotifications = repository.allNotifications.asLiveData()

    fun syncCloud(userEmail: String) {
        viewModelScope.launch {
            // 🔥 PERBAIKAN: Gunakan nama fungsi yang benar sesuai Repository 🔥

            // 1. Ambil Info Pesanan (Nama di Repo: syncPersonalOrders)
            repository.syncPersonalOrders(userEmail)

            // 2. Ambil Promo (Nama di Repo: syncPromosFromAdmin)
            // Perhatikan huruf 's' di Promos
            repository.syncPromosFromAdmin()
        }
    }

    fun markAsRead(id: String) {
        repository.updateNotificationReadStatus(id, true)
    }
}