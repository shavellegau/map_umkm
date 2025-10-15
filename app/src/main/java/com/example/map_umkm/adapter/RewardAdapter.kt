package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map_umkm.databinding.ItemRewardBinding
import com.example.map_umkm.model.Reward

class RewardAdapter(private val rewardList: List<Reward>) :
    RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    inner class RewardViewHolder(val binding: ItemRewardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewardList[position]
        with(holder.binding) {
            title.text = reward.title
            point.text = "${reward.point} Poin"
            Glide.with(imageView.context)
                .load(reward.imageResId)
                .into(imageView)
        }
    }

    override fun getItemCount() = rewardList.size
}
