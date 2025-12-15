// File: com/example/map_umkm/adapter/RewardAdapter.kt
package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Reward

class RewardAdapter(
    private val rewards: List<Reward>,
    // Tambahkan listener untuk menangani klik penukaran
    private val onRedeemClick: (Reward) -> Unit
) :
    RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val title: TextView = itemView.findViewById(R.id.title)
        val point: TextView = itemView.findViewById(R.id.point)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewards[position]

        // --- FITUR YANG SUDAH ADA ---
        holder.imageView.setImageResource(reward.imageResId)
        holder.title.text = reward.title
        holder.point.text = "${reward.point} Poin"
        // ---------------------------

        // 🔥 INTEGRASI LOGIKA KLIK PENUKARAN 🔥
        holder.itemView.setOnClickListener {
            onRedeemClick(reward)
        }
    }

    override fun getItemCount(): Int = rewards.size
}