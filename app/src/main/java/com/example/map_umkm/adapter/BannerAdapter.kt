package com.example.map_umkm.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R

class BannerAdapter(private val bannerList: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgBanner: ImageView = itemView.findViewById(R.id.bannerImage)
        init {
            Log.d("BannerAdapter", "ViewHolder dibuat. ID Image ditemukan.")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        Log.d("BannerAdapter", "Layout item_banner di-inflate.")
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.imgBanner.setImageResource(bannerList[position])
        Log.d("BannerAdapter", "Gambar dimuat untuk posisi: $position, Resource ID: ${bannerList[position]}")
    }

    override fun getItemCount() = bannerList.size
}