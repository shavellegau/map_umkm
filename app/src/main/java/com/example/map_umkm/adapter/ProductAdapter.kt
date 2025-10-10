package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map_umkm.R
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.Locale

class   ProductAdapter(
    private var products: MutableList<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnAddToCart: ImageButton = itemView.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // --- Set nama dan harga ---
        holder.tvProductName.text = product.name ?: "Produk"
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(product.price_hot ?: 0.0)
        holder.tvProductPrice.text = formattedPrice

        // --- Muat gambar menggunakan Glide ---
        val imageUrl = product.image
        Glide.with(holder.itemView.context)
            .load(imageUrl ?: R.drawable.default_image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.ivProductImage)

        // --- Klik tombol tambah ---
        holder.btnAddToCart.setOnClickListener {
            onProductClick(product)
        }

        // --- Klik seluruh item untuk buka detail produk ---
        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    // --- Untuk memperbarui data ---
    fun updateData(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}
