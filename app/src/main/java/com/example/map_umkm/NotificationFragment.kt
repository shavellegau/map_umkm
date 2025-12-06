package com.example.map_umkm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.NotificationAdapter
import com.example.map_umkm.databinding.FragmentNotificationBinding
import com.example.map_umkm.AppDatabase
import com.example.map_umkm.repository.NotificationRepository
import com.example.map_umkm.viewmodel.NotificationViewModel
import com.example.map_umkm.viewmodel.NotificationViewModelFactory
import com.example.map_umkm.model.Notification // Model Tampilan
import com.example.map_umkm.model.NotificationEntity // Model Database

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel
    private val notificationViewModel: NotificationViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = NotificationRepository(database.notificationDao())
        NotificationViewModelFactory(repository)
    }

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup Adapter
        notificationAdapter = NotificationAdapter(emptyList())
        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }

        // 🔥 LANGKAH KRUSIAL: PANGGIL SYNC DARI CLOUD SAAT FRAGMENT DIBUKA 🔥
        triggerCloudSync()

        // Observasi Data (Otomatis update jika Room berubah setelah sync)
        observeNotifications()
    }

    private fun triggerCloudSync() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("userEmail", null)

        if (userEmail != null) {
            // Panggil fungsi sync di ViewModel
            notificationViewModel.syncCloud(userEmail)
        } else {
            // Opsional: Handle jika belum login
            // Toast.makeText(context, "User belum login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeNotifications() {
        // 🔥 Tambahkan tipe data eksplisit (List<NotificationEntity>) agar compiler tidak bingung
        notificationViewModel.allNotifications.observe(viewLifecycleOwner) { notifications: List<NotificationEntity> ->

            if (notifications.isNotEmpty()) {
                binding.tvEmptyNotification.visibility = View.GONE
                binding.recyclerNotifications.visibility = View.VISIBLE

                // Mapping data Entity ke Model Tampilan Adapter
                val displayList = notifications.map { entity ->
                    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    val timestampString = formatter.format(Date(entity.timestamp))

                    Notification(
                        title = entity.title,
                        body = entity.body,
                        timestamp = timestampString
                    )
                }

                notificationAdapter.updateList(displayList)
            } else {
                binding.recyclerNotifications.visibility = View.GONE
                binding.tvEmptyNotification.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}