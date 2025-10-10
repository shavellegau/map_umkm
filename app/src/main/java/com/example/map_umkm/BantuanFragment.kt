package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.map_umkm.databinding.FragmentBantuanBinding

class BantuanFragment : Fragment() {

    private var _binding: FragmentBantuanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBantuanBinding.inflate(inflater, container, false)
        val view = binding.root

        // Tombol Chat Langsung
        binding.btnChatLangsung.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka Chat Langsung...", Toast.LENGTH_SHORT).show()
        }

        // Tombol Kirim Email
        binding.btnKirimEmail.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka Email...", Toast.LENGTH_SHORT).show()
        }

        // Klik item Produk & Toko
        binding.itemProdukToko.setOnClickListener {
            Toast.makeText(requireContext(), "Buka Bantuan Produk & Toko", Toast.LENGTH_SHORT).show()
        }

        // Klik item Pesanan Maya
        binding.itemPesananMaya.setOnClickListener {
            Toast.makeText(requireContext(), "Buka Bantuan Pesanan Maya", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
