//package com.example.map_umkm.helper
//
//import com.google.firebase.firestore.FirebaseFirestore
//
//object FirestoreHelper {
//
//    private val db = FirebaseFirestore.getInstance()
//
//    fun addToWishlist(
//        productId: String,
//        productName: String,
//        productPrice: String,
//        imageUrl: String,
//        callback: (Boolean) -> Unit
//    ) {
//        val userId = "user_demo" // TODO: ganti dengan UID user login nantinya
//        val wishlistRef = db.collection("users")
//            .document(userId)
//            .collection("wishlist")
//            .document(productId)
//
//        val data = hashMapOf(
//            "id" to productId,
//            "name" to productName,
//            "price" to productPrice,
//            "imageUrl" to imageUrl
//        )
//
//        wishlistRef.set(data)
//            .addOnSuccessListener { callback(true) }
//            .addOnFailureListener { callback(false) }
//    }
//
//    object FirestoreHelper {
//        private val db = FirebaseFirestore.getInstance()
//
//        fun addToWishlist(userId: String, product: Product, onComplete: (Boolean) -> Unit) {
//            db.collection("users").document(userId)
//                .collection("wishlist")
//                .document(product.id)
//                .set(product)
//                .addOnSuccessListener { onComplete(true) }
//                .addOnFailureListener { onComplete(false) }
//        }
//
//        fun removeFromWishlist(userId: String, productId: String, onComplete: (Boolean) -> Unit) {
//            db.collection("users").document(userId)
//                .collection("wishlist")
//                .document(productId)
//                .delete()
//                .addOnSuccessListener { onComplete(true) }
//                .addOnFailureListener { onComplete(false) }
//        }
//
//        fun getWishlist(userId: String, onComplete: (List<Map<String, Any>>) -> Unit) {
//            db.collection("users").document(userId)
//                .collection("wishlist")
//                .get()
//                .addOnSuccessListener { result ->
//                    onComplete(result.documents.mapNotNull { it.data })
//                }
//                .addOnFailureListener {
//                    onComplete(emptyList())
//                }
//        }
//    }
//
//}
