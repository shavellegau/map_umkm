package com.example.map_umkm.helper

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    fun addToWishlist(
        productId: String,
        productName: String,
        productPrice: String,
        imageUrl: String,
        callback: (Boolean) -> Unit
    ) {
        val userId = "user_demo" // TODO: ganti dengan UID user login nantinya
        val wishlistRef = db.collection("users")
            .document(userId)
            .collection("wishlist")
            .document(productId)

        val data = hashMapOf(
            "id" to productId,
            "name" to productName,
            "price" to productPrice,
            "imageUrl" to imageUrl
        )

        wishlistRef.set(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun removeFromWishlist(productId: String, callback: (Boolean) -> Unit) {
        val userId = "user_demo"
        val wishlistRef = db.collection("users")
            .document(userId)
            .collection("wishlist")
            .document(productId)

        wishlistRef.delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getWishlist(callback: (List<Map<String, Any>>) -> Unit) {
        val userId = "user_demo"
        db.collection("users")
            .document(userId)
            .collection("wishlist")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.data }
                callback(list)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
}
