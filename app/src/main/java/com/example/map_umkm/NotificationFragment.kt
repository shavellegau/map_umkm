package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels 
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.map_umkm.databinding.FragmentNotificationBinding
import com.example.map_umkm.viewmodel.NotificationViewModel
import com.example.map_umkm.viewmodel.NotificationViewModelFactory
import com.example.map_umkm.repository.NotificationRepository
import com.example.map_umkm.AppDatabase
import com.google.android.material.tabs.TabLayoutMediator

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    
    private val notificationViewModel: NotificationViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = NotificationRepository(database.notificationDao())
        NotificationViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        
        
        try {
            binding.root.findViewById<View>(R.id.btnBack).setOnClickListener {
                findNavController().popBackStack()
            }
        } catch (e: Exception) { /* Handle if view not found */ }

        
        val pagerAdapter = NotifPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Info Pesanan" else "Promo"
        }.attach()

        
        triggerCloudSync()
    }

    private fun triggerCloudSync() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", android.content.Context.MODE_PRIVATE)
        val userEmail = prefs.getString("userEmail", null)
        if (userEmail != null) {
            notificationViewModel.syncCloud(userEmail)
        }
    }

    
    inner class NotifPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            
            val type = if (position == 0) "INFO" else "PROMO"
            return NotificationListFragment.newInstance(type)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}