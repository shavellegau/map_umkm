// File: com/example/map_umkm/utils/PointService.kt
package com.example.map_umkm.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Transaction
import android.util.Log

/**
 * Class untuk menangani semua logika bisnis terkait Poin dengan keamanan data yang tinggi.
 * Poin didapat: Total Belanja * 3% (dibulatkan ke bawah).
 */
object PointService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private const val USERS_COLLECTION = "users"
    private const val TRANSACTIONS_COLLECTION = "point_transactions"

    /**
     * Menghitung poin (3%), menambah saldo user secara atomic, dan mencatat log.
     */
    fun calculateAndAddPoints(
        totalPurchase: Double,
        orderId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "User tidak terautentikasi.")
            return
        }

        // Hitung poin: 3% dari total pembelian
        val pointsToAdd = (totalPurchase * 0.10).toLong()

        if (pointsToAdd <= 0) {
            onComplete(true, "Pembelian Rp ${"%.0f".format(totalPurchase)} tidak menghasilkan poin.")
            return
        }

        // Transaksi Batch: Update saldo dan catat log harus sukses bersamaan (all-or-nothing)
        db.runBatch { batch ->
            // A. Update Saldo Poin User (Menggunakan FieldValue.increment)
            val userRef = db.collection(USERS_COLLECTION).document(userId)
            batch.update(userRef, "points", FieldValue.increment(pointsToAdd))

            // B. Buat Log Transaksi Poin
            val transactionData = hashMapOf(
                "userId" to userId,
                "type" to "EARNED",
                "amount" to pointsToAdd,
                "orderId" to orderId,
                "description" to "Poin dari pembelian order $orderId",
                "timestamp" to Timestamp.now()
            )
            val transactionRef = db.collection(TRANSACTIONS_COLLECTION).document()
            batch.set(transactionRef, transactionData)
        }
            .addOnSuccessListener {
                onComplete(true, "Sukses menambahkan $pointsToAdd Poin.")
            }
            .addOnFailureListener { e ->
                Log.e("PointService", "Gagal memproses poin: ${e.message}")
                onComplete(false, "Gagal memproses poin: ${e.message}")
            }
    }

    /**
     * Mengurangi poin user saat redeem reward.
     */
    fun redeemPoints(
        pointsToRedeem: Long,
        rewardTitle: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "User tidak terautentikasi.")
            return
        }

        val userRef = db.collection(USERS_COLLECTION).document(userId)

        // Menggunakan Transaction untuk membaca saldo (cek kecukupan) dan mengurangi secara aman
        db.runTransaction { transaction: Transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("points") ?: 0L

            if (currentPoints < pointsToRedeem) {
                // Melemparkan exception akan membatalkan transaksi
                throw Exception("Saldo poin tidak cukup ($currentPoints) untuk menukarkan $pointsToRedeem poin.")
            }

            // A. Update Saldo Poin
            transaction.update(userRef, "points", FieldValue.increment(-pointsToRedeem))

            // B. Buat Log Transaksi Poin
            val transactionData = hashMapOf(
                "userId" to userId,
                "type" to "REDEEMED",
                "amount" to -pointsToRedeem, // Simpan negatif
                "orderId" to "REWARD-${System.currentTimeMillis()}",
                "description" to "Tukar poin untuk $rewardTitle",
                "timestamp" to Timestamp.now()
            )
            val transactionRef = db.collection(TRANSACTIONS_COLLECTION).document()
            transaction.set(transactionRef, transactionData)

            null // Transaksi sukses
        }
            .addOnSuccessListener {
                onComplete(true, "Berhasil menukarkan $pointsToRedeem Poin untuk $rewardTitle.")
            }
            .addOnFailureListener { e ->
                Log.e("PointService", "Gagal redeem poin: ${e.message}")
                onComplete(false, e.message ?: "Gagal menukarkan poin.")
            }
    }
}