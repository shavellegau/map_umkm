package com.example.map_umkm.adapter

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

        val context = holder.itemView.context

        Glide.with(context)
            .load(item.image) // Muat URL/Path gambar dari model Product
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.ivProductImage)


        holder.tvProductName.text = item.name

        val finalPrice = (if (item.selectedType == "iced") item.price_iced else item.price_hot) ?: 0
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            .format(finalPrice.toLong())
        holder.tvProductPrice.text = formattedPrice

        holder.tvQuantity.text = item.quantity.toString()

        if (!item.notes.isNullOrBlank()) {
            holder.tvNotes.text = "Catatan: ${item.notes}"
            holder.tvNotes.visibility = View.VISIBLE
        } else {
            holder.tvNotes.visibility = View.GONE
        }

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
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Jumlah minimal adalah 1",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.btnDelete.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null)
            val dialog = Dialog(context)
            dialog.setContentView(dialogView)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

            tvMessage.text = "Apakah kamu yakin ingin menghapus ${item.name} dari keranjang?"

            btnCancel.setOnClickListener { dialog.dismiss() }
            btnDelete.setOnClickListener {
                onDeleteItem(item)
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    fun updateItems(newItems: List<Product>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}