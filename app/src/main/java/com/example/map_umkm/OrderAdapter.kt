package com.example.map_umkm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

data class Order(
    val type: String,
    val place: String,
    val items: String,
    val price: String,
    val date: String
)

class OrdersAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type = view.findViewById<TextView>(R.id.tvType)
        val place = view.findViewById<TextView>(R.id.tvPlace)
        val items = view.findViewById<TextView>(R.id.tvItems)
        val price = view.findViewById<TextView>(R.id.tvPrice)
        val date = view.findViewById<TextView>(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.type.text = order.type
        holder.place.text = order.place
        holder.items.text = order.items
        holder.price.text = order.price
        holder.date.text = order.date
    }

    override fun getItemCount(): Int = orders.size
}
