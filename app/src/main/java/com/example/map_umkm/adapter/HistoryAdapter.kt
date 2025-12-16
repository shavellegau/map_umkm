package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.History

class HistoryAdapter(private var historyList: MutableList<History> = mutableListOf()) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_history_title)
        val tvAmount: TextView = view.findViewById(R.id.tv_history_amount)
        val imgIcon: ImageView = view.findViewById(R.id.img_history_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_sample, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        // 1. Gunakan 'title' sesuai model Anda
        holder.tvTitle.text = item.title

        // 2. Cek tipe transaksi untuk warna & simbol
        val isEarn = item.type == "earn"
        val prefix = if (isEarn) "+" else "-"

        // 3. Gunakan 'point' sesuai model Anda (bukan amount/points)
        holder.tvAmount.text = "$prefix${item.point} Pts"

        // 4. Atur Warna
        val colorRes = if (isEarn) R.color.tuku_primary else android.R.color.holo_red_dark
        holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        // 5. Atur Gambar (Manual logic karena Firestore tidak simpan Int ID)
        val iconRes = if (isEarn) R.drawable.ic_point else R.drawable.ic_voucher
        holder.imgIcon.setImageResource(iconRes)
    }

    override fun getItemCount() = historyList.size

    fun updateData(newHistoryList: List<History>) {
        historyList.clear()
        historyList.addAll(newHistoryList)
        notifyDataSetChanged()
    }
}