package com.example.map_umkm.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private var orderList: MutableList<Product>,
    private val onCartChanged: (List<Product>) -> Unit // biar bisa update total di PaymentFragment
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val product = orderList[position]

        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(product.price_hot * product.quantity)

        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = formattedPrice
        holder.tvQuantity.text = product.quantity.toString()

        // Tombol tambah
        holder.btnPlus.setOnClickListener {
            product.quantity++
            notifyItemChanged(position)
            onCartChanged(orderList)
        }

        // Tombol kurang
        holder.btnMinus.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity--
                notifyItemChanged(position)
                onCartChanged(orderList)
            }
        }

        // Tombol hapus dengan konfirmasi
        holder.btnDelete.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Hapus Pesanan")
                .setMessage("Apakah kamu yakin ingin menghapus pesanan ini?")
                .setPositiveButton("Ya") { dialog, _ ->
                    orderList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, orderList.size)
                    onCartChanged(orderList)
                    dialog.dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    override fun getItemCount(): Int = orderList.size
}
