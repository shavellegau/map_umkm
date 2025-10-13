package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.bumptech.glide.Glide
import com.example.map_umkm.model.Product

class OrderItemsAdapter(
    private val items: List<Product>
) : RecyclerView.Adapter<OrderItemsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage) // Deklarasi ImageView
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail_product, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        // Memuat gambar produk menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.placeholder_image) // Gambar sementara
            .error(R.drawable.error_image) // Gambar jika gagal
            .into(holder.ivProductImage)

        holder.tvProductName.text = item.name
        holder.tvQuantity.text = "Jumlah: x${item.quantity}"

        if (!item.notes.isNullOrBlank()) {
            holder.tvNotes.text = "Catatan: ${item.notes}"
        } else {
            holder.tvNotes.text = "Catatan: -"
        }
    }
}