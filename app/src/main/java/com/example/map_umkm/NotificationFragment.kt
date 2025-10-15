package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.NotificationAdapter
import com.example.map_umkm.databinding.FragmentNotificationBinding
import com.example.map_umkm.model.Notification

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol kembali
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // ===== Dummy data =====
        val dummyNotifications = listOf(
            Notification(
                title = "Pesanan Selesai!",
                body = "Pesanan Anda dengan ID #12345 telah selesai.",
                timestamp = "14 Okt 2025, 12:00"
            ),
            Notification(
                title = "Pesanan Dikirim",
                body = "Pesanan Anda sedang dalam perjalanan.",
                timestamp = "13 Okt 2025, 18:32"
            ),
            Notification(
                title = "Pembayaran Berhasil",
                body = "Pembayaran untuk pesanan #12345 telah dikonfirmasi.",
                timestamp = "13 Okt 2025, 17:15"
            ),
            Notification(
                title = "Promo Baru!",
                body = "Nikmati diskon 20% untuk produk UMKM pilihan minggu ini!",
                timestamp = "12 Okt 2025, 10:05"
            )
        )

        // Setup RecyclerView
        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = NotificationAdapter(dummyNotifications)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
