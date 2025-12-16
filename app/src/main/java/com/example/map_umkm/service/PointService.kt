package com.example.map_umkm.service

import com.example.map_umkm.model.Reward
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object PointService {
    private val db = FirebaseFirestore.getInstance()

    fun redeemReward(
        reward: Reward,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().uid ?: return onFail("Not logged in")
        val userDoc = db.collection("users").document(uid)

        db.runTransaction { trx ->
            val snapshot = trx.get(userDoc)
            // Nama field di database firebase harus konsisten (misal: "tukuPoints")
            val currentPoints = snapshot.getLong("tukuPoints") ?: 0

            // [FIX] Gunakan reward.point (Singular) sesuai model Anda
            if (currentPoints < reward.point) {
                throw Exception("Poin tidak cukup!")
            }

            // Kurangi poin user
            trx.update(userDoc, "tukuPoints", currentPoints - reward.point)

            // Simpan history
            val newHistoryRef = userDoc.collection("point_history").document()

            // [FIX] Map keys disesuaikan dengan model History Anda
            val historyData = mapOf(
                "title" to reward.title,   // Sesuai model: title
                "point" to reward.point,   // Sesuai model: point
                "type" to "redeem",
                "timestamp" to Timestamp.now()
            )

            trx.set(newHistoryRef, historyData)

        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onFail(e.message ?: "Terjadi kesalahan saat penukaran")
        }
    }
}