package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Notification

class NotificationAdapter(private var notificationList: List<Notification>) : // 1. Ganti 'val' menjadi 'var'
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_notification_title)
        val tvBody: TextView = itemView.findViewById(R.id.tv_notification_body)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_notification_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.tvTitle.text = notification.title
        holder.tvBody.text = notification.body
        holder.tvTimestamp.text = notification.timestamp
    }

    override fun getItemCount(): Int = notificationList.size

    // 2. 🔔 FUNGSI BARU: updateList()
    /**
     * Memperbarui daftar notifikasi dengan data terbaru dari Room Database.
     * Fungsi ini dibutuhkan oleh NotificationFragment.kt.
     */
    fun updateList(newList: List<Notification>) {
        // Ganti daftar lama dengan daftar baru
        this.notificationList = newList

        // Memberi tahu RecyclerView untuk me-reload tampilannya
        notifyDataSetChanged()
    }
}