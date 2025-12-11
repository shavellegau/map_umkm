package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.databinding.ItemHistoryBinding
import com.example.map_umkm.model.HistoryModel
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : ListAdapter<HistoryModel, HistoryAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<HistoryModel>() {
            override fun areItemsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
                return oldItem.timestamp == newItem.timestamp && oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: HistoryModel, newItem: HistoryModel): Boolean {
                return oldItem == newItem
            }
        }

        private val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryModel) {
            // title
            binding.tvTitle.text = item.title

            // points (+ or -)
            binding.tvPoint.text = item.points.toString()
            binding.tvPoint.setTextColor(
                if (item.points >= 0) 0xFF388E3C.toInt() else 0xFFC62828.toInt()
            )

            // date
            binding.tvDate.text =
                if (item.timestamp > 0) sdf.format(Date(item.timestamp)) else ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
