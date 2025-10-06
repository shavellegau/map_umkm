package com.example.map_umkm.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Product

class ProductAdapter(
    private var products: List<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvOldPrice: TextView = itemView.findViewById(R.id.tvOldPrice)
        val btnAdd: ImageView = itemView.findViewById(R.id.btnAdd) // tombol +
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvName.text = product.name
        holder.tvPrice.text = product.price
        holder.tvOldPrice.text = product.oldPrice ?: ""

        // Coret harga lama
        holder.tvOldPrice.paintFlags =
            holder.tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Logika untuk menampilkan gambar atau placeholder
        // Jika ada resource gambar, tampilkan. Jika tidak, tampilkan placeholder.
        if (product.imageRes != 0) {
            holder.ivProduct.setImageResource(product.imageRes)
            // Hapus latar belakang placeholder
            holder.ivProduct.setBackgroundResource(android.R.color.transparent)
        } else {
            // Tampilkan placeholder yang lebih baik (misalnya ikon default)
            holder.ivProduct.setImageResource(R.drawable.banner_kopi)
        }

        // Hanya tombol '+' yang memiliki listener untuk menambahkan item
        holder.btnAdd.setOnClickListener { onClick(product) }
    }

    override fun getItemCount() = products.size

    // Menggunakan DiffUtil untuk update data yang lebih efisien (SANGAT DISARANKAN)
    // Untuk ini, Anda perlu membuat ProductDiffCallback.kt
    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged() // Perlu diubah ke DiffUtil untuk performa terbaik
    }
}