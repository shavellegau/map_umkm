package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map_umkm.databinding.ItemHistoryBinding
import com.example.map_umkm.model.History

class HistoryAdapter(private val historyList: List<History>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]
        with(holder.binding) {
            title.text = history.title
            point.text = "${history.point} Poin"
            Glide.with(imageView.context)
                .load(history.imageResId)
                .into(imageView)
        }
    }

    override fun getItemCount() = historyList.size
}
