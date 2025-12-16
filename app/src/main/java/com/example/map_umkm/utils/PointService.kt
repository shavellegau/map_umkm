package com.example.map_umkm.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import android.util.Log

object PointService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun calculateAndAddPoints(
        totalPurchase: Double,
        orderId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        // Fix logika 3% (0.03)
        val pointsToAdd = (totalPurchase * 0.03).toLong()

        if (pointsToAdd <= 0) {
            onComplete(true, "Tidak ada poin ditambahkan.")
            return
        }

        db.runBatch { batch ->
            val userRef = db.collection("users").document(userId)
            // Pakai FieldValue.increment agar tidak perlu baca data lama (menghindari error null)
            batch.update(userRef, "points", FieldValue.increment(pointsToAdd))

            val logRef = db.collection("point_transactions").document()
            batch.set(logRef, hashMapOf(
                "userId" to userId,
                "amount" to pointsToAdd,
                "type" to "EARNED",
                "timestamp" to Timestamp.now()
            ))
        }.addOnSuccessListener {
            onComplete(true, "Dapat $pointsToAdd Poin")
        }.addOnFailureListener {
            onComplete(false, it.message)
        }
    }

    // Fungsi Redeem (Penting untuk mengatasi error di Fragment lain)
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
                    "timestamp" to Timestamp.now()
                ))
            } else {
                throw Exception("Poin tidak cukup")
            }
        }.addOnSuccessListener {
            onComplete(true, "Berhasil tukar poin")
        }.addOnFailureListener {
            onComplete(false, it.message)
        }
    }
}