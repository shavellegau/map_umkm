package com.example.map_umkm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map_umkm.data.CartManager
import com.example.map_umkm.model.Product

class CartViewModel : ViewModel() {

    // LiveData menyimpan isi keranjang dari CartManager
    private val _cartList = MutableLiveData<MutableList<Product>>(CartManager.getItems())
    val cartList: LiveData<MutableList<Product>> get() = _cartList

    // 🔹 Tambahkan ke keranjang
    fun addToCart(product: Product, selectedType: String, notes: String? = null) {
        // Gunakan CartManager agar data global tersimpan dengan catatan juga
        CartManager.addItem(product, selectedType, notes)
        // Perbarui LiveData agar UI ikut update
        _cartList.value = CartManager.getItems()
    }

    // 🔹 Kurangi jumlah produk
    fun removeFromCart(product: Product) {
        CartManager.removeItem(product)
        _cartList.value = CartManager.getItems()
    }

    // 🔹 Hapus item sepenuhnya
    fun deleteItem(product: Product) {
        CartManager.deleteItem(product)
        _cartList.value = CartManager.getItems()
    }

    // 🔹 Kosongkan semua isi keranjang
    fun clearCart() {
        CartManager.clear()
        _cartList.value = CartManager.getItems()
    }
}
