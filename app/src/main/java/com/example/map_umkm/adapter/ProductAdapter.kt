package com.example.map_umkm.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

    private var favoriteIds: Set<String> = emptySet()

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

        // ---------- PRICE ----------
        val priceToShow = product.price_hot ?: product.price_iced ?: 0
        holder.tvProductPrice.text = NumberFormat
            .getCurrencyInstance(Locale("in", "ID"))
            .format(priceToShow.toLong())

        // ---------- LOG IMAGE URL (WAJIB) ----------
        Log.d(
            "IMAGE_CHECK",
            "Product: ${product.name}, image = '${product.image}'"
        )

        // ---------- GLIDE IMAGE ----------
        val imageUrl = product.image

        if (!imageUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.logo_tuku)
                .error(R.drawable.error_image)
                .centerCrop()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(
                            "GLIDE_ERROR",
                            "FAILED: ${product.name} | $imageUrl",
                            e
                        )
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(
                            "GLIDE_SUCCESS",
                            "SUCCESS: ${product.name}"
                        )
                        return false
                    }
                })
                .into(holder.ivProductImage)
        } else {
            // IMAGE NULL / KOSONG
            Log.e(
                "IMAGE_CHECK",
                "IMAGE NULL/KOSONG -> ${product.name}"
            )
            holder.ivProductImage.setImageResource(R.drawable.logo_tuku)
        }

        // ---------- FAVORITE ----------
        val isFav = favoriteIds.contains(product.id) || product.isFavorite
        holder.btnFavorite.setImageResource(
            if (isFav)
                R.drawable.ic_favorite_filled
            else
                R.drawable.ic_favorite_border
        )

        holder.btnFavorite.setOnClickListener {
            onFavoriteToggle(product, !isFav)
        }

        // ---------- CART ----------
        holder.btnAddToCart.setOnClickListener {
            onAddToCartClick(product)
        }

        // ---------- ITEM CLICK ----------
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

    fun updateFavorites(newFavoriteIds: Set<String>) {
        favoriteIds = newFavoriteIds
        for ((index, product) in products.withIndex()) {
            val isNowFav = favoriteIds.contains(product.id)
            if (product.isFavorite != isNowFav) {
                product.isFavorite = isNowFav
                notifyItemChanged(index)
            }
        }
    }
}
