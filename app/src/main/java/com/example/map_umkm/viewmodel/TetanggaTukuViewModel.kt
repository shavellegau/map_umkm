package com.example.map_umkm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.model.MembershipReward
import com.example.map_umkm.model.UserData
import com.example.map_umkm.utils.TierCalculator
import com.example.map_umkm.utils.TierInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class TetanggaTukuViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData

    private val _tierInfo = MutableLiveData<TierInfo>()
    val tierInfo: LiveData<TierInfo> = _tierInfo

    // LiveData untuk List Rewards
    private val _rewards = MutableLiveData<List<MembershipReward>>()
    val rewards: LiveData<List<MembershipReward>> = _rewards

    fun loadData() {
        val userId = auth.currentUser?.uid ?: return // Pastikan user login

        // 1. Ambil Data User
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    var user = document.toObject(UserData::class.java)

                    if (user != null) {
                        // LOGIKA POINT DECAY (BERKURANG)
                        // Cek apakah sudah lebih dari 30 hari sejak transaksi terakhir?
                        user = checkAndApplyPointDecay(user, userId)

                        _userData.value = user!!

                        // Hitung Tier berdasarkan XP saat ini
                        val currentTier = TierCalculator.calculateTier(user.currentXp)
                        _tierInfo.value = currentTier
                    }
                }
            }

        // 2. Ambil Data Rewards
        db.collection("rewards").get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(MembershipReward::class.java)
                _rewards.value = list
            }
    }

    private fun checkAndApplyPointDecay(user: UserData, userId: String): UserData {
        val lastDate = user.lastTransactionDate?.toDate() ?: return user

        // Hitung selisih hari
        val diff = Date().time - lastDate.time
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()

        if (days > 30 && user.currentPoints > 0) {
            // Logika: Kurangi 10% point jika > 30 hari tidak aktif
            val newPoints = (user.currentPoints * 0.9).toInt()

            // Update ke Firebase
            db.collection("users").document(userId)
                .update("currentPoints", newPoints)

            // Return data baru untuk update UI langsung
            return user.copy(currentPoints = newPoints)
        }
        return user
    }
}