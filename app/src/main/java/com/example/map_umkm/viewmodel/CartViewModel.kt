package com.example.map_umkm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.model.Product // [FIXED] Impor model Product yang benar

class CartViewModel : ViewModel() {

    // [FIXED] Menggunakan tipe data Product dari proyek Anda
    private val _cartList = MutableLiveData<MutableList<Product>>(mutableListOf())
    val cartList: LiveData<MutableList<Product>> get() = _cartList

    fun addToCart(product: Product, type: String) {
        val currentList = _cartList.value ?: mutableListOf()
        val existingItem = currentList.find { it.id == product.id && it.selectedType == type }

        if (existingItem != null) {
            existingItem.quantity++
        } else {
            val newItem = product.copy(quantity = 1, selectedType = type)
            currentList.add(newItem)
        }
        _cartList.value = currentList
    }

    fun removeFromCart(productId: String, type: String) {
        val currentList = _cartList.value ?: return
        val item = currentList.find { it.id == productId && it.selectedType == type }

        if (item != null) {
            if (item.quantity > 1) {
                item.quantity--
            } else {
                currentList.remove(item)
            }
            _cartList.value = currentList
        }
    }

    // Fungsi untuk menghapus item sepenuhnya (misal dari tombol silang)
    fun deleteItem(product: Product) {
        val currentList = _cartList.value ?: return
        currentList.remove(product)
        _cartList.value = currentList
    }

    fun clearCart() {
        _cartList.value = mutableListOf()
    }
}
