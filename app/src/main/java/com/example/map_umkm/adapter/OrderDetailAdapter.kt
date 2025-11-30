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

        holder.tvProductName.text = item.name
        // [FIXED] Tampilkan quantity dengan format yang lebih jelas
        holder.tvQuantity.text = "Jumlah: x${item.quantity}"

        // Tampilkan catatan jika ada
        if (item.notes.isNullOrBlank()) {
            holder.tvNotes.visibility = View.GONE
        } else {
            holder.tvNotes.visibility = View.VISIBLE
            holder.tvNotes.text = "Catatan: ${item.notes}"
        }

        // Memuat gambar produk dari drawable secara dinamis
        val context = holder.itemView.context
        val imageResId = context.resources.getIdentifier(item.image, "drawable", context.packageName)

        Glide.with(context)
            .load(if (imageResId != 0) imageResId else R.drawable.placeholder_image) // Muat gambar lokal, fallback ke placeholder
            .placeholder(R.drawable.logo_tuku) // Placeholder saat loading
            .error(R.drawable.logo_tuku) // Gambar jika terjadi error (misal, nama file di JSON salah)
            .into(holder.ivProductImage)
    }

    override fun getItemCount() = items.size
}
