package com.example.map_umkm.data

import com.example.map_umkm.model.Product

object CartManager {
    private val cartItems = mutableListOf<Product>()

    fun getItems(): MutableList<Product> = cartItems

    
    fun addItem(product: Product, selectedType: String, notes: String? = null) {
        val existing = cartItems.find { it.id == product.id && it.selectedType == selectedType }

        if (existing != null) {
            existing.quantity++
            
            if (!notes.isNullOrEmpty()) {
                existing.notes = notes
            }
        } else {
            
            val newItem = product.copy(selectedType = selectedType, quantity = 1, notes = notes)
            cartItems.add(newItem)
        }
    }

    fun removeItem(product: Product) {
        val existing = cartItems.find { it.id == product.id && it.selectedType == product.selectedType }
        if (existing != null) {
            existing.quantity--
            if (existing.quantity <= 0) {
                cartItems.remove(existing)
            }
        }
    }

    fun deleteItem(product: Product) {
        cartItems.removeAll { it.id == product.id && it.selectedType == product.selectedType }
    }

    fun clear() {
        cartItems.clear()
    }

    fun getTotalPrice(): Int {
        return cartItems.sumOf {
            val price = (if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0
            price * it.quantity
        }
    }
}
