package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.databinding.ItemRewardBinding
import com.example.map_umkm.model.Reward
import com.example.map_umkm.service.PointService

class RewardAdapter(private val rewardList: List<Reward>) :
    RecyclerView.Adapter<RewardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRewardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = rewardList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reward = rewardList[position]
        val ctx = holder.itemView.context

        holder.binding.apply {
            title.text = reward.name
            point.text = "${reward.points} Poin"
            imageView.setImageResource(reward.image)
        }

        holder.itemView.setOnClickListener {
            PointService.redeemReward(
                reward,
                onSuccess = {
                    Toast.makeText(ctx, "Berhasil Redeem!", Toast.LENGTH_SHORT).show()
                },
                onFail = { msg ->
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
