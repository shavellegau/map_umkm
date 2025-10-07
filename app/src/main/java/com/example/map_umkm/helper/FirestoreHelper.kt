package com.example.map_umkm.helper

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    fun getWishlist(callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("wishlist")
            .get()
            .addOnSuccessListener { result ->
                val dataList = result.documents.mapNotNull { it.data }
                callback(dataList)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}