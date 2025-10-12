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
import java.util.*

class AdminOrdersAdapter(
    private var orders: List<Order>,
    private val onProsesClick: (Order) -> Unit,
    private val onSelesaikanClick: (Order) -> Unit
) : RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserInfo: TextView = view.findViewById(R.id.tv_order_user_info)
        val tvDate: TextView = view.findViewById(R.id.tv_order_date)
        val tvItems: TextView = view.findViewById(R.id.tv_order_items)
        val tvTotal: TextView = view.findViewById(R.id.tv_order_total)
        val tvStatus: TextView = view.findViewById(R.id.tv_order_status)
        val btnProses: Button = view.findViewById(R.id.btn_proses_pesanan)
        val btnSelesaikan: Button = view.findViewById(R.id.btn_selesaikan_pesanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val itemNames = order.items.joinToString(", ") { "${it.name} (x${it.quantity})" }

        holder.tvUserInfo.text = "${order.userName} (${order.userEmail})"
        holder.tvDate.text = order.orderDate
        holder.tvItems.text = itemNames
        holder.tvTotal.text = "Total: ${currencyFormat.format(order.totalAmount)}"
        holder.tvStatus.text = "Status: ${order.status}"

        holder.btnProses.setOnClickListener { onProsesClick(order) }
        holder.btnSelesaikan.setOnClickListener { onSelesaikanClick(order) }

        when (order.status) {
            "Menunggu Konfirmasi" -> {
                holder.btnProses.visibility = View.VISIBLE
                holder.btnSelesaikan.visibility = View.GONE
            }
            "Diproses" -> {
                holder.btnProses.visibility = View.GONE
                holder.btnSelesaikan.visibility = View.VISIBLE
            }
            else -> { // Selesai
                holder.btnProses.visibility = View.GONE
                holder.btnSelesaikan.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
