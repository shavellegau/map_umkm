package com.example.map_umkm.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _favoriteProducts = MutableLiveData<List<Product>>(emptyList())
    val favoriteProducts: LiveData<List<Product>> = _favoriteProducts


    fun loadFavorites() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .addSnapshotListener { value, _ ->
                val list = value?.toObjects(Product::class.java) ?: emptyList()
                _favoriteProducts.value = list
            }
    }

    fun addFavorite(product: Product) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .document(product.id)
            .set(product.copy(isFavorite = true))
    }

    fun removeFavorite(product: Product) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .document(product.id)
            .delete()
    }

    fun isFavorite(productId: String): Boolean {
        return _favoriteProducts.value?.any { it.id == productId } == true
    }

    fun toggleFavorite(product: Product) {
        if (isFavorite(product.id)) removeFavorite(product)
        else addFavorite(product)
    }
}
