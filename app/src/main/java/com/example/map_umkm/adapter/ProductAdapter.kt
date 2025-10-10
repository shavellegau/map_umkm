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

class ProductAdapter(
    private var products: MutableList<Product>,
    private val onProductClick: (Product) -> Unit,
    private val onFavoriteToggle: (Product, Boolean) -> Unit // ✅ Tambahan listener favorit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnAddToCart: ImageButton = itemView.findViewById(R.id.btnAddToCart)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite) // ✅ Tambahan tombol favorite
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvProductName.text = product.name ?: "Produk"

        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(product.price_hot) // Menggunakan price_hot karena itu harga utama

        holder.tvProductPrice.text = formattedPrice

        // Muat gambar dari URL menggunakan Glide
        product.image?.let { imageUrl ->
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.ivProductImage)
        } ?: run {
            // Muat gambar default jika URL null
            Glide.with(holder.itemView.context).load(R.drawable.default_image).into(holder.ivProductImage)
        }

        // ✅ Atur icon favorite sesuai status
        if (product.isFavorite) {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
        }

        // ✅ Listener untuk tombol favorite
        holder.btnFavorite.setOnClickListener {
            val newState = !product.isFavorite
            product.isFavorite = newState
            onFavoriteToggle(product, newState)
            notifyItemChanged(holder.adapterPosition)
        }

        // Listener untuk klik item produk
        holder.itemView.setOnClickListener {
            onProductClick(product)
        }

        // Listener untuk tombol cart (tetap seperti semula)
        holder.btnAddToCart.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}

