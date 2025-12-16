package com.example.map_umkm.service

import com.example.map_umkm.model.Reward
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

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
            val currentPoints = snapshot.getLong("points") ?: 0

            if (currentPoints < reward.points) {
                throw Exception("Point tidak cukup!")
            }

            trx.update(userDoc, "points", currentPoints - reward.points)

            trx.set(
                userDoc.collection("histories").document(),
                mapOf(
                    "title" to reward.name,
                    "points" to reward.points,
                    "type" to "redeem",
                    "timestamp" to Timestamp.now()
                )
            )
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFail(e.message ?: "Error!") }
    }
}
