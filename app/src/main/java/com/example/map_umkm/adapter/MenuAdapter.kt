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

class MenuAdapter(
    private var items: List<Product>,
    private val onFavoriteClick: (Product) -> Unit,
    private val isFavorite: (Product) -> Boolean
) : RecyclerView.Adapter<MenuAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.tvProductName)
        val productImage: ImageView = view.findViewById(R.id.ivProductImage)
        val productPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val product = items[position]
        holder.productName.text = product.name
        holder.productPrice.text = "Rp ${product.price_hot ?: product.price_iced ?: 0}"

        Glide.with(holder.itemView.context)
            .load(product.image)
            .placeholder(R.drawable.logo_tuku)
            .into(holder.productImage)

        // Cek apakah produk sudah favorite
        if (isFavorite(product)) {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_outline)
        }

        holder.btnFavorite.setOnClickListener {
            onFavoriteClick(product)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<Product>) {
        items = newList
        notifyDataSetChanged()
    }
}
