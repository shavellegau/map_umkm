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
    private val onFavoriteToggle: (Product, Boolean) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ID ini disesuaikan dengan layout item
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnAddToCart: ImageButton = itemView.findViewById(R.id.btnAddToCart)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Ganti 'item_menu' ke 'item_product' atau sebaliknya jika perlu
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvProductName.text = product.name

        val priceToShow = product.price_hot ?: product.price_iced ?: 0
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(priceToShow.toLong())
        holder.tvProductPrice.text = formattedPrice

        val context = holder.itemView.context
        val imageResId = context.resources.getIdentifier(product.image, "drawable", context.packageName)

        Glide.with(context)
            .load(if (imageResId != 0) imageResId else R.drawable.placeholder_image)
            .placeholder(R.drawable.logo_tuku)
            .error(R.drawable.error_image)
            .into(holder.ivProductImage)

        holder.btnFavorite.setImageResource(
            if (product.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )

        holder.btnFavorite.setOnClickListener {
            val newState = !product.isFavorite
            onFavoriteToggle(product, newState)
            product.isFavorite = newState
            notifyItemChanged(position)
        }

        holder.btnAddToCart.setOnClickListener {
            onAddToCartClick(product)
        }

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    fun updateFavorites(favoriteIds: Set<String>) {
        for (product in products) {
            product.isFavorite = favoriteIds.contains(product.id)
        }
        notifyDataSetChanged()
    }
}
