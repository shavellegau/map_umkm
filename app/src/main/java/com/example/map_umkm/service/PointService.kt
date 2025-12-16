package com.example.map_umkm.service // Sesuaikan package dengan folder

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

object PointService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Fungsi Hitung Poin (Otomatis nambah saat beli)
    fun calculateAndAddPoints(
        totalPurchase: Double,
        orderId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val pointsToAdd = (totalPurchase * 0.03).toLong() // 3% dari total

        if (pointsToAdd <= 0) {
            onComplete(true, "Tidak ada poin")
            return
        }

        db.runBatch { batch ->
            val userRef = db.collection("users").document(userId)
            batch.update(userRef, "points", FieldValue.increment(pointsToAdd))

            val logRef = db.collection("point_transactions").document()
            batch.set(logRef, hashMapOf(
                "userId" to userId,
                "amount" to pointsToAdd,
                "type" to "EARNED",
                "desc" to "Order $orderId",
                "timestamp" to Timestamp.now()
            ))
        }.addOnSuccessListener {
            onComplete(true, "Dapat $pointsToAdd Poin")
        }.addOnFailureListener {
            onComplete(false, it.message)
        }
    }

    // Fungsi Tukar Poin (Dipakai di Fragment Tukar Poin)
    fun redeemPoints(
        pointsToRedeem: Long,
        rewardTitle: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("points") ?: 0L

            if (currentPoints >= pointsToRedeem) {
                transaction.update(userRef, "points", FieldValue.increment(-pointsToRedeem))

                val logRef = db.collection("point_transactions").document()
                transaction.set(logRef, hashMapOf(
                    "userId" to userId,
                    "amount" to -pointsToRedeem,
                    "type" to "REDEEMED",
                    "desc" to "Tukar $rewardTitle",
                    "timestamp" to Timestamp.now()
                ))
            } else {
                throw Exception("Poin tidak cukup")
            }
        }.addOnSuccessListener {
            onComplete(true, "Berhasil menukar $rewardTitle")
        }.addOnFailureListener {
            onComplete(false, "Gagal: ${it.message}")
        }
    }
}