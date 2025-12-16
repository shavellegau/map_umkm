//package com.example.map_umkm.data
//
//import com.example.map_umkm.model.MembershipReward
//import com.google.firebase.firestore.FirebaseFirestore
//
//class TukuRepository {
//
//    private val db = FirebaseFirestore.getInstance()
//
//    fun getTiers(onResult: (List<MemberTier>) -> Unit) {
//        db.collection("tiers")
//            .orderBy("order")
//            .get()
//            .addOnSuccessListener {
//                onResult(it.toObjects(MemberTier::class.java))
//            }
//    }
//
//    fun getRewards(onResult: (List<MembershipReward>) -> Unit) {
//        db.collection("rewards")
//            .get()
//            .addOnSuccessListener {
//                onResult(it.toObjects(MembershipReward::class.java))
//            }
//    }
//}
