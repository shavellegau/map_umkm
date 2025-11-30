package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // <-- PENTING: Tambah impor ini
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.NotificationAdapter
import com.example.map_umkm.databinding.FragmentNotificationBinding
// Import komponen Room
import com.example.map_umkm.AppDatabase
import com.example.map_umkm.repository.NotificationRepository
import com.example.map_umkm.viewmodel.NotificationViewModel
import com.example.map_umkm.viewmodel.NotificationViewModelFactory
import com.example.map_umkm.model.Notification // Import model Room

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    // 1. Deklarasi ViewModel
    private val notificationViewModel: NotificationViewModel by viewModels {
        // Inisialisasi Database dan Repository
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

        // 2. Setup Adapter dengan list kosong
        notificationAdapter = NotificationAdapter(emptyList())

        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }

        // 3. HAPUS DUMMY DATA dan GANTI DENGAN OBSERVASI LIVE DATA
        observeNotifications()
    }

    private fun observeNotifications() {
        notificationViewModel.allNotifications.observe(viewLifecycleOwner) { notifications ->

            if (notifications.isNotEmpty()) {
                binding.tvEmptyNotification.visibility = View.GONE
                binding.recyclerNotifications.visibility = View.VISIBLE

                // Konversi Room Entity ke Model yang digunakan Adapter (Notification)
                val displayList = notifications.map { entity ->
                    // Format timestamp (Long) menjadi String tanggal yang bagus
                    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    val timestampString = formatter.format(Date(entity.timestamp))

                    // Asumsi Model Notification Anda menerima title, body, dan timestamp String
                    Notification(
                        title = entity.title,
                        body = entity.body,
                        timestamp = timestampString
                    )
                }

                // Panggil fungsi update pada Adapter
                notificationAdapter.updateList(displayList)
            } else {
                // Tampilkan pesan kosong jika tidak ada notifikasi
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