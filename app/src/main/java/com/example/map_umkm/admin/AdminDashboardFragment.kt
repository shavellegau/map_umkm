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


import com.example.map_umkm.AdminMenuFragment
import com.example.map_umkm.AdminOrdersFragment
import com.example.map_umkm.AdminVoucherFragment
import com.example.map_umkm.admin.AdminNotificationFragment
import com.example.map_umkm.admin.AdminCabangFragment 
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
        
        val fragments = listOf(
            AdminOrdersFragment(),
            AdminMenuFragment(),
            AdminVoucherFragment(),
            AdminCabangFragment(),     
            AdminNotificationFragment() 
        )

        
        val titles = listOf(
            "Pesanan",    
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

            
            
            
            offscreenPageLimit = fragments.size
        }

        
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
        
        val customView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_logout_confirm, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .setCancelable(true) 
            .create()

        
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
        
        binding.adminViewPager.adapter = null
        super.onDestroyView()
        _binding = null
    }
}