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
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        // ✅ Tambahkan ini untuk menampilkan catatan
        val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        // Gambar produk
        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.ivProductImage)

        holder.tvProductName.text = item.name

        // Harga
        val finalPrice = (if (item.selectedType == "iced") item.price_iced else item.price_hot) ?: 0
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            .format(finalPrice.toLong())
        holder.tvProductPrice.text = formattedPrice

        // Jumlah
        holder.tvQuantity.text = item.quantity.toString()

        // ✅ Tampilkan catatan jika ada
        if (!item.notes.isNullOrBlank()) {
            holder.tvNotes.text = "Catatan: ${item.notes}"
            holder.tvNotes.visibility = View.VISIBLE
        } else {
            holder.tvNotes.visibility = View.GONE
        }

        // Tombol tambah
        holder.btnPlus.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            onQuantityChanged()
        }

        // Tombol kurang
        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
                onQuantityChanged()
            } else {
                onDeleteItem(item)
            }
        }

        // Tombol hapus
        holder.btnDelete.setOnClickListener { onDeleteItem(item) }
    }

    fun updateItems(newItems: List<Product>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
