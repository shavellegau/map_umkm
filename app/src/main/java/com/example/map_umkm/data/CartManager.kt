package com.example.map_umkm.data

import com.example.map_umkm.model.Product

object CartManager {
    private val cartItems = mutableListOf<Product>()

    fun getItems(): MutableList<Product> = cartItems

    fun addItem(product: Product) {
        val existing = cartItems.find { it.id == product.id && it.selectedType == product.selectedType }
        if (existing != null) {
            existing.quantity++
        } else {
            cartItems.add(product.copy(quantity = 1))
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
            // 🔥 Pilih harga sesuai jenis minuman (hot / iced)
            val price = if (it.selectedType == "iced") it.price_iced ?: it.price_hot else it.price_hot
            price * it.quantity
        }
    }
}
