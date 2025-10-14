package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.map_umkm.databinding.FragmentPengaturanAkunBinding

class PengaturanAkunFragment : Fragment() {

    private var _binding: FragmentPengaturanAkunBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengaturanAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // contoh: tombol kembali ke profil
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // contoh: listener lainnya
        binding.btnUbahPassword.setOnClickListener {
            // aksi ubah password (nanti bisa diarahkan ke fragment lain juga)
        }

        binding.btnHapusAkun.setOnClickListener {
            // aksi hapus akun
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
