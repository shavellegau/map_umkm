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
import androidx.navigation.fragment.findNavController // Diperlukan untuk Navigasi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.map_umkm.AdminMenuFragment
import com.example.map_umkm.AdminOrdersFragment
import com.example.map_umkm.AdminVoucherFragment
import com.example.map_umkm.admin.AdminCabangFragment
import com.example.map_umkm.LoginActivity // Asumsi class LoginActivity ada
import com.example.map_umkm.R // Diperlukan untuk ID resource
import com.example.map_umkm.databinding.FragmentAdminDashboardBinding
import com.google.android.material.tabs.TabLayoutMediator


class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    // Menggunakan properti backing untuk menghindari NullPointerException setelah onDestroyView
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
        setupLogout() // Memanggil fungsi setupLogout
    }

    private fun setupTabs() {
        val fragments = listOf(
            AdminOrdersFragment(),
            AdminMenuFragment(),
            AdminVoucherFragment(),
            AdminCabangFragment()
        )

        val titles = listOf(
            "Konfirmasi Pesanan",
            "Manajemen Menu",
            "Tambah Voucher",
            "Tambah Cabang"
        )

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        binding.adminViewPager.adapter = adapter

        TabLayoutMediator(binding.adminTabLayout, binding.adminViewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun setupLogout() {
        // Asumsi tombol logout berada di dalam FragmentAdminDashboardBinding dan memiliki ID btnLogout
        binding.btnLogout.setOnClickListener {
            showCustomLogoutDialog()
        }
    }

    private fun showCustomLogoutDialog() {
        // Inflate layout kustom dialog_logout_confirm.xml
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_logout_confirm, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .create()

        // Asumsi di dialog_logout_confirm.xml terdapat tombol dengan ID btnConfirmLogout dan btnCancelLogout
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
        // 1. Hapus Session Admin
        val sharedPreferences = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // 2. Navigasi kembali ke LoginActivity dan clear back stack
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()

        // Pilihan Alternatif (Hanya jika LoginActivity sudah merupakan Host Navigasi)
        // try {
        //     findNavController().navigate(R.id.action_adminDashboard_to_login)
        // } catch (e: Exception) {
        //     e.printStackTrace()
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus referensi binding untuk mencegah memory leak
        _binding = null
    }
}