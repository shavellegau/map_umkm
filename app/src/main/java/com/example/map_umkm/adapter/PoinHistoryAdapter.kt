// File: com/example/map_umkm/adapter/PoinHistoryAdapter.kt
package com.example.map_umkm.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.PoinHistory

class PoinHistoryAdapter(private val historyList: List<PoinHistory>) :
    RecyclerView.Adapter<PoinHistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // MENGGUNAKAN ID DARI XML ANDA (item_poin_history.xml)
        val tvDescription: TextView = view.findViewById(R.id.txtTitle)
        val tvAmount: TextView = view.findViewById(R.id.txtAmount)
        val tvDate: TextView = view.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // MERUJUK KE FILE LAYOUT ITEM_POIN_HISTORY.XML
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poin_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        holder.tvDescription.text = item.title
        holder.tvAmount.text = item.amount
        holder.tvDate.text = item.date

        // Logika pewarnaan berdasarkan tanda (+) atau (-)
        val color = if (item.amount.startsWith("+")) {
            // Jika poin bertambah (Hijau/Teal)
            Color.parseColor("#008577")
        } else {
            // 🔥 SOLUSI R.color.design_default_color_error: Diganti dengan Color.RED
            // Jika poin berkurang (Merah)
            Color.RED
        }
        holder.tvAmount.setTextColor(color)
    }

    override fun getItemCount() = historyList.size
}