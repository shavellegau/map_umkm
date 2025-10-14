package com.example.map_umkm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.model.Product

class FavoriteViewModel : ViewModel() {

    // LiveData publik untuk list favorite (immutable dari luar)
    private val _favoriteProducts = MutableLiveData<List<Product>>(emptyList())
    val favoriteProducts: LiveData<List<Product>> = _favoriteProducts

    fun addFavorite(product: Product) {
        val current = _favoriteProducts.value.orEmpty().toMutableList()
        if (current.none { it.id == product.id }) {
            // Simpan salinan dengan flag isFavorite true agar state konsisten
            val p = product.copy(isFavorite = true)
            current.add(p)
            _favoriteProducts.value = current
        }
    }

    fun removeFavorite(product: Product) {
        val current = _favoriteProducts.value.orEmpty().toMutableList()
        val changed = current.removeAll { it.id == product.id }
        if (changed) {
            _favoriteProducts.value = current
        }
    }

    fun isFavorite(productId: String): Boolean {
        return _favoriteProducts.value?.any { it.id == productId } == true
    }

    // Optional: toggle
    fun toggleFavorite(product: Product) {
        if (isFavorite(product.id)) removeFavorite(product) else addFavorite(product)
    }
}
