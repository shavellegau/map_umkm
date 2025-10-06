package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.Locale

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

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.ivProductImage.setImageResource(item.imageRes)
        holder.tvProductName.text = item.name
        holder.tvProductPrice.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.price)
        holder.tvQuantity.text = "1"

        var quantity = 1

        holder.btnPlus.setOnClickListener {
            quantity++
            holder.tvQuantity.text = quantity.toString()
            onQuantityChanged()
        }

        holder.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                holder.tvQuantity.text = quantity.toString()
                onQuantityChanged()
            }
        }

        holder.btnDelete.setOnClickListener { onDeleteItem(item) }
    }

    override fun getItemCount(): Int = items.size
}
