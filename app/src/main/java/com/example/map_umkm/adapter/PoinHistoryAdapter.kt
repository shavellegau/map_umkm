package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.databinding.ItemHistoryBinding
import com.example.map_umkm.model.PoinHistory

class PoinHistoryAdapter(private var items: List<PoinHistory>) :
    RecyclerView.Adapter<PoinHistoryAdapter.VH>() {

    inner class VH(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(h: PoinHistory) {

            binding.tvTitle.text = h.title
            binding.tvPoint.text = h.amount

            val isPositive = h.amount.startsWith("+")
            binding.tvPoint.setTextColor(
                if (isPositive) 0xFF388E3C.toInt() else 0xFFC62828.toInt()
            )

            binding.tvDate.text = h.date
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    fun updateData(newItems: List<PoinHistory>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
