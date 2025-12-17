package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.TierModel

class TierAdapter(private val tierList: List<TierModel>) :
    RecyclerView.Adapter<TierAdapter.TierViewHolder>() {

    inner class TierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon: ImageView = itemView.findViewById(R.id.img_tier_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_tier_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TierViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tier_card, parent, false)

        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        return TierViewHolder(view)
    }

    override fun onBindViewHolder(holder: TierViewHolder, position: Int) {
        val tier = tierList[position]
        holder.tvName.text = tier.name
        holder.imgIcon.setImageResource(tier.imageResId)
    }

    override fun getItemCount(): Int = tierList.size
}