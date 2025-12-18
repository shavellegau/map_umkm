package com.example.map_umkm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.data.CartManager
import com.example.map_umkm.model.Product

class CartViewModel : ViewModel() {

    
    private val _cartList = MutableLiveData<MutableList<Product>>(CartManager.getItems())
    val cartList: LiveData<MutableList<Product>> get() = _cartList

    
    fun addToCart(product: Product, selectedType: String, notes: String? = null) {
        
        CartManager.addItem(product, selectedType, notes)
        
        _cartList.value = CartManager.getItems()
    }

    
    fun removeFromCart(product: Product) {
        CartManager.removeItem(product)
        _cartList.value = CartManager.getItems()
    }

    
    fun deleteItem(product: Product) {
        CartManager.deleteItem(product)
        _cartList.value = CartManager.getItems()
    }

    
    fun clearCart() {
        CartManager.clear()
        _cartList.value = CartManager.getItems()
    }
}
