package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.BroadcastHistory
import java.text.SimpleDateFormat
import java.util.Locale

// Tambahkan parameter onItemClick di constructor
class BroadcastAdapter(
    private var list: List<BroadcastHistory>,
    private val onItemClick: (BroadcastHistory) -> Unit // Callback klik
) : RecyclerView.Adapter<BroadcastAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_notification_title)
        val tvBody: TextView = view.findViewById(R.id.tv_notification_body)
        val tvDate: TextView = view.findViewById(R.id.tv_notification_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTitle.text = item.title
        holder.tvBody.text = item.body

        val date = item.timestamp?.toDate()
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        holder.tvDate.text = if (date != null) format.format(date) else "-"

        // 🔥 Pasang Listener Klik 🔥
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<BroadcastHistory>) {
        list = newList
        notifyDataSetChanged()
    }
}