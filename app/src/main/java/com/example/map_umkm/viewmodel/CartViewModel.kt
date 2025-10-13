package com.example.map_umkm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.data.CartManager // [FIXED] Import CartManager
import com.example.map_umkm.model.Product

class CartViewModel : ViewModel() {

    // [FIXED] Saat pertama kali dibuat, langsung ambil data dari CartManager
    private val _cartList = MutableLiveData<MutableList<Product>>(CartManager.getItems())
    val cartList: LiveData<MutableList<Product>> get() = _cartList

    // Fungsi-fungsi ini sekarang hanya akan jadi "jembatan" ke CartManager
    // dan memastikan LiveData diperbarui.

    fun addToCart(product: Product, type: String) {
        // Buat item baru dengan tipe yang dipilih
        val newItem = product.copy(selectedType = type)
        CartManager.addItem(newItem) // Tambahkan ke manager
        _cartList.value = CartManager.getItems() // Perbarui LiveData dari manager
    }

    fun removeFromCart(product: Product) { // [FIXED] Disederhanakan, cukup terima product
        CartManager.removeItem(product) // Hapus dari manager
        _cartList.value = CartManager.getItems() // Perbarui LiveData dari manager
    }

    fun deleteItem(product: Product) {
        CartManager.deleteItem(product) // Hapus dari manager
        _cartList.value = CartManager.getItems() // Perbarui LiveData dari manager
    }

    fun clearCart() {
        CartManager.clear() // Bersihkan manager
        _cartList.value = CartManager.getItems() // Perbarui LiveData (jadi kosong)
    }
}
