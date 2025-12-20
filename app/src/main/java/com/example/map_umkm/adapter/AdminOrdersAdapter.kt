package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Order
import java.text.NumberFormat
import java.util.Locale

class AdminOrdersAdapter(

    private var orderList: List<Order>,
    private val onItemClick: (Order) -> Unit,
    private val onConfirmPaymentClick: (Order) -> Unit,
    private val onAntarPesananClick: (Order) -> Unit,
    private val onSelesaikanClick: (Order) -> Unit
)  : RecyclerView.Adapter<AdminOrdersAdapter.AdminOrderViewHolder>() {

    class AdminOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserInfo: TextView = itemView.findViewById(R.id.tv_order_user_info)
        val tvDate: TextView = itemView.findViewById(R.id.tv_order_date)
        val tvItems: TextView = itemView.findViewById(R.id.tv_order_items)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_order_status)

        val btnConfirm: Button = itemView.findViewById(R.id.btn_konfirmasi_pembayaran)
        val btnSelesai: Button = itemView.findViewById(R.id.btn_selesaikan_pesanan) // Re-used for different actions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_admin, parent, false)
        return AdminOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = orderList[position]

        val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val itemNames = order.items.joinToString(", ") { "${it.name} (x${it.quantity})" }

        holder.tvUserInfo.text = "${order.userName} (${order.userEmail})"
        holder.tvDate.text = order.orderDate
        holder.tvItems.text = itemNames
        holder.tvTotal.text = formatRp.format(order.totalAmount)
        holder.tvStatus.text = order.status

        // Hide all buttons by default
        holder.btnConfirm.visibility = View.GONE
        holder.btnSelesai.visibility = View.GONE

        // Show buttons based on order status
        when (order.status) {
            "Menunggu Pembayaran", "Menunggu Konfirmasi" -> {
                holder.btnConfirm.visibility = View.VISIBLE
                holder.btnConfirm.setOnClickListener { onConfirmPaymentClick(order) }
            }
            "Diproses" -> {
                holder.btnSelesai.visibility = View.VISIBLE
                if (order.orderType == "Delivery") {
                    holder.btnSelesai.text = "Antar Pesanan"
                    holder.btnSelesai.setOnClickListener { onAntarPesananClick(order) }
                } else { // Take Away
                    holder.btnSelesai.text = "Selesaikan Pesanan"
                    holder.btnSelesai.setOnClickListener { onSelesaikanClick(order) }
                }
            }
            "Dikirim" -> { // Only for Delivery
                holder.btnSelesai.visibility = View.VISIBLE
                holder.btnSelesai.text = "Selesaikan Pesanan"
                holder.btnSelesai.setOnClickListener { onSelesaikanClick(order) }
            }
            // No buttons for "Selesai" or "Dibatalkan"
        }

        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    override fun getItemCount(): Int = orderList.size

    fun updateData(newOrders: List<Order>) {
        orderList = newOrders
        notifyDataSetChanged()
    }
}