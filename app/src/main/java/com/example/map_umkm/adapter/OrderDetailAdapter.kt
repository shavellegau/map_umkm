package com.example.map_umkm.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map_umkm.R
import com.example.map_umkm.model.Product

class OrderDetailAdapter(private val items: List<Product>) :
    RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvNotes: TextView = view.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // 1. Set Nama & Jumlah
        holder.tvProductName.text = item.name
        holder.tvQuantity.text = "Jumlah: x${item.quantity}"

        // 2. Set Catatan
        if (item.notes.isNullOrEmpty()) {
            holder.tvNotes.visibility = View.GONE
        } else {
            holder.tvNotes.visibility = View.VISIBLE
            holder.tvNotes.text = "Catatan: ${item.notes}"
        }

        // 🔥 3. LOAD GAMBAR LANGSUNG (SAMA SEPERTI CART ITEM) 🔥
        Log.d("ORDER_ADAPTER", "Loading Image: ${item.image}") // Cek log ini

        Glide.with(context)
            .load(item.image) // Langsung muat path/url dari object
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.ivProductImage)
    }

    override fun getItemCount() = items.size
}