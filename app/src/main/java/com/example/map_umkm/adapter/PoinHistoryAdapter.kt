package com.example.map_umkm.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.PoinHistory

// Ubah constructor menjadi var agar bisa diupdate
class PoinHistoryAdapter(private var historyList: List<PoinHistory> = listOf()) :
    RecyclerView.Adapter<PoinHistoryAdapter.HistoryViewHolder>() {

    // [SOLUSI ERROR submitList] Tambahkan fungsi ini manual
    fun updateData(newList: List<PoinHistory>) {
        historyList = newList
        notifyDataSetChanged()
    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.txtTitle)
        val tvAmount: TextView = view.findViewById(R.id.txtAmount)
        val tvDate: TextView = view.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poin_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        holder.tvDescription.text = item.title
        holder.tvAmount.text = item.amount
        holder.tvDate.text = item.date

        // Logika warna: Hijau jika ada tanda "+", Merah jika tidak
        if (item.amount.contains("+")) {
            holder.tvAmount.setTextColor(Color.parseColor("#008577")) // Hijau Teal
        } else {
            holder.tvAmount.setTextColor(Color.RED)
        }
    }

    override fun getItemCount() = historyList.size
}