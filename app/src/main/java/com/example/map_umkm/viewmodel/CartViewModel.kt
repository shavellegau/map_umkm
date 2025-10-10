package com.example.map_umkm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.model.Product

class CartViewModel : ViewModel() {

    private val _cartList = MutableLiveData<MutableList<Product>>(mutableListOf())
    val cartList: LiveData<MutableList<Product>> = _cartList

    // This function adds a single product or increments its quantity
    fun addProduct(product: Product) {
        val currentList = _cartList.value ?: mutableListOf()
        val existingProduct = currentList.find { it.id == product.id }

        if (existingProduct != null) {
            existingProduct.quantity++
        } else {
            currentList.add(product.copy(quantity = 1))
        }
        _cartList.value = currentList
    }

    // This function updates the entire list (used by PaymentFragment)
    fun updateCartList(newCartList: List<Product>) {
        _cartList.value = newCartList.toMutableList()
    }

    fun clearCart() {
        _cartList.value?.clear()
    }
}