package com.example.map_umkm.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.map_umkm.AdminMenuFragment // Akan kita buat
import com.example.map_umkm.AdminOrdersFragment // Akan kita buat

class AdminPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminMenuFragment()
            1 -> AdminOrdersFragment()
            else -> throw IllegalStateException("Posisi tidak valid: $position")
        }
    }
}
