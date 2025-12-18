package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.OnboardingItem


class OnboardingAdapter(private val onboardingItems: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    
    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageOnboarding: ImageView = view.findViewById(R.id.ivOnboarding)
        private val textTitle: TextView = view.findViewById(R.id.tvTitle)
        private val textDescription: TextView = view.findViewById(R.id.tvDesc)

        fun bind(onboardingItem: OnboardingItem) {
            imageOnboarding.setImageResource(onboardingItem.image)
            textTitle.text = onboardingItem.title
            textDescription.text = onboardingItem.description
        }
    }

    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        return OnboardingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        )
    }

    
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    
    override fun getItemCount(): Int = onboardingItems.size
}