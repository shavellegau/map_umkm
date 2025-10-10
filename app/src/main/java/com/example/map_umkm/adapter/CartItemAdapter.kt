package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map_umkm.R
import java.text.NumberFormat
import java.util.Locale
import com.example.map_umkm.model.Product


class CartItemAdapter(
    private val items: MutableList<Product>,
    private val onQuantityChanged: () -> Unit,
    private val onDeleteItem: (Product) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val btnPlus: Button = itemView.findViewById(R.id.btnPlus)
        val btnMinus: Button = itemView.findViewById(R.id.btnMinus)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        // Load image: Use Glide to load the image from the URL.
        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.placeholder_image) // Shows placeholder while loading
            .error(R.drawable.error_image) // Shows error image if load fails
            .into(holder.ivProductImage)

        holder.tvProductName.text = item.name

        // Show the price for the chosen temperature (hot or iced)
        val finalPrice = if (item.price_iced != null && item.price_iced != 0) {
            item.price_iced
        } else {
            item.price_hot
        }

        val formattedPrice =
            NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(finalPrice)
        holder.tvProductPrice.text = formattedPrice

        holder.tvQuantity.text = item.quantity.toString()

        holder.btnPlus.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            onQuantityChanged()
        }

        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
                onQuantityChanged()
            }
        }

        holder.btnDelete.setOnClickListener { onDeleteItem(item) }
    }
}