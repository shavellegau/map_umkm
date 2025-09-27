package com.example.map_umkm

import android.content.Intent
import com.example.map_umkm.OrderDetailActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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

        // Klik → buka OrderDetailActivity
        holder.itemView.setOnClickListener { v ->
            val ctx = v.context
            val intent = Intent(ctx, OrderDetailActivity::class.java).apply {
                putExtra("ORDER_TYPE", order.type)
                putExtra("ORDER_PLACE", order.place)
                putExtra("ORDER_ITEMS", order.items)
                putExtra("ORDER_PRICE", order.price)
                putExtra("ORDER_DATE", order.date)
            }
            ctx.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size
}
