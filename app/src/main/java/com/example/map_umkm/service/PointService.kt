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
        val uid = FirebaseAuth.getInstance().uid

        // Cek login
        if (uid == null) {
            onFail("User belum login")
            return
        }

        val userDoc = db.collection("users").document(uid)

        db.runTransaction { trx ->
            val snapshot = trx.get(userDoc)
            // Ambil poin user saat ini dari Firestore
            val currentPoints = snapshot.getLong("points") ?: 0

            // [PERBAIKAN] Menggunakan reward.point (sesuai Model Reward)
            if (currentPoints < reward.point) {
                throw Exception("Poin tidak mencukupi!")
            }

            // [PERBAIKAN] Menggunakan reward.point
            val sisaPoin = currentPoints - reward.point
            trx.update(userDoc, "points", sisaPoin)

            // Catat Riwayat
            val newHistoryRef = userDoc.collection("histories").document()

            val historyMap = hashMapOf(
                // [PERBAIKAN] Menggunakan reward.title (sesuai Model Reward)
                "title" to "Tukar: ${reward.title}",
                // [PERBAIKAN] Menggunakan reward.point
                "points" to reward.point,
                "type" to "redeem",
                "timestamp" to Timestamp.now()
            )

            trx.set(newHistoryRef, historyMap)

        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onFail(e.message ?: "Terjadi kesalahan transaksi")
        }
    }
}