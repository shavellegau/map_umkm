package com.example.map_umkm.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class BroadcastHistory(
    @DocumentId
    var id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Timestamp? = null
)