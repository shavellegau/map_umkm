package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: MutableList<Product>, // biar bisa di-update
    private val onAddToCart: (Product, Boolean) -> Unit, // ✅ dua parameter
    private val onFavoriteToggle: (Product, Boolean) -> Unit // ✅ ditambahkan
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnAddToCart: ImageButton = itemView.findViewById(R.id.btnAddToCart)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvProductName.text = product.name
        holder.tvProductPrice.text =
            NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(product.price)
        holder.ivProductImage.setImageResource(product.imageRes)

        // Ganti icon hati sesuai status favorit
        holder.btnFavorite.setImageResource(
            if (product.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )

        // ✅ klik tombol favorite
        holder.btnFavorite.setOnClickListener {
            product.isFavorite = !product.isFavorite
            notifyItemChanged(position)
            onFavoriteToggle(product, product.isFavorite)
        }

        // ✅ klik tombol tambah ke cart
        holder.btnAddToCart.setOnClickListener {
            onAddToCart(product, true) // ← tambahkan parameter kedua Boolean
        }
    }

    override fun getItemCount(): Int = products.size

    // ✅ Update list dengan aman
    fun updateData(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}
