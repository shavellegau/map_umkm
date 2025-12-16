package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.History

class HistoryAdapter(
    private var historyList: List<History> = listOf()
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // Gunakan findViewById agar aman dan pasti dikenali
    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_history_title)
        val tvDate: TextView = view.findViewById(R.id.tv_history_date)
        val tvPoint: TextView = view.findViewById(R.id.tv_history_point)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Pastikan nama layout XML sesuai dengan Tahap 1
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_point, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.tvTitle.text = item.title
        holder.tvDate.text = item.date
        holder.tvPoint.text = "${item.point} Poin"
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newList: List<History>) {
        historyList = newList
        notifyDataSetChanged()
    }
}