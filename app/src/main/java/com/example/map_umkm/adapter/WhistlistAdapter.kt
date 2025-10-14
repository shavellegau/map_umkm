package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map_umkm.R
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.*

class WishlistAdapter(
    private var favoriteList: List<Product>,
    private val onProductClick: (Product) -> Unit,
    private val onFavoriteToggle: (Product, Boolean) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(product: Product) {
            tvName.text = product.name
            val price = product.price_hot ?: product.price_iced ?: 0
            tvPrice.text = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(price)
            Glide.with(itemView.context).load(product.image).into(ivImage)

            ivFavorite.setImageResource(
                if (product.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )

            itemView.setOnClickListener {
                onProductClick(product) // ✅ klik card → buka detail
            }

            ivFavorite.setOnClickListener {
                val newStatus = !product.isFavorite
                onFavoriteToggle(product, newStatus)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favoriteList[position])
    }

    override fun getItemCount(): Int = favoriteList.size

    fun updateData(newList: List<Product>) {
        favoriteList = newList
        notifyDataSetChanged()
    }
}
