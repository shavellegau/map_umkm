package com.example.map_umkm.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator

// --- IMPORT FRAGMENT (Pastikan Package Sesuai Lokasi File Anda) ---
import com.example.map_umkm.AdminMenuFragment
import com.example.map_umkm.AdminOrdersFragment
import com.example.map_umkm.AdminVoucherFragment
import com.example.map_umkm.admin.AdminNotificationFragment
import com.example.map_umkm.admin.AdminCabangFragment // Ini yang tadi kita fix
import com.example.map_umkm.LoginActivity
import com.example.map_umkm.R
import com.example.map_umkm.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupLogout()
    }

    private fun setupTabs() {
        // Daftar Fragment untuk setiap Tab
        val fragments = listOf(
            AdminOrdersFragment(),
            AdminMenuFragment(),
            AdminVoucherFragment(),
            AdminCabangFragment(),     // Tab Cabang (Sudah diperbaiki)
            AdminNotificationFragment() // Tab Broadcast
        )

        // Judul Tab
        val titles = listOf(
            "Pesanan",    // Disingkat agar muat di layar HP
            "Menu",
            "Voucher",
            "Cabang",
            "Broadcast"
        )

        val adapter = object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        binding.adminViewPager.apply {
            this.adapter = adapter

            // OPTIMASI PENTING:
            // Menyimpan state 4 halaman di kiri/kanan agar tidak reload data saat digeser.
            // Menghemat kuota Firebase dan membuat aplikasi terasa cepat.
            offscreenPageLimit = fragments.size
        }

        // Hubungkan Tab Layout dengan ViewPager
        TabLayoutMediator(binding.adminTabLayout, binding.adminViewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            showCustomLogoutDialog()
        }
    }

    private fun showCustomLogoutDialog() {
        // Pastikan layout 'dialog_logout_confirm' benar-benar ada di folder res/layout
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_logout_confirm, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .setCancelable(true) // Bisa ditutup dengan klik area luar
            .create()

        // Pastikan ID tombol di XML dialog_logout_confirm sesuai
        val btnConfirm = customView.findViewById<Button>(R.id.btnLogout)
        val btnCancel = customView.findViewById<Button>(R.id.btnCancel)

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            logoutAdminSession()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logoutAdminSession() {
        val sharedPreferences = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        // Hapus adapter agar tidak memory leak saat view dihancurkan
        binding.adminViewPager.adapter = null
        super.onDestroyView()
        _binding = null
    }
}