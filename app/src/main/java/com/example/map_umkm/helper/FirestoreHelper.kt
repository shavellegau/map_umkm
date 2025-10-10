package com.example.map_umkm.helper

import com.example.map_umkm.model.Product
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

    fun addToWishlist(product: Product, onComplete: (Boolean) -> Unit) {
        // Asumsi dokumen diidentifikasi dengan ID produk
        val wishlistRef = db.collection("wishlist").document(product.id.toString())

        // Simpan data produk ke Firestore
        wishlistRef.set(product)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun removeFromWishlist(product: Product, onComplete: (Boolean) -> Unit) {
        val wishlistRef = db.collection("wishlist").document(product.id.toString())

        // Hapus dokumen dari Firestore
        wishlistRef.delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}