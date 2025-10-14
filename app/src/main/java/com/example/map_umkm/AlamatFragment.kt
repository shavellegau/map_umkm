package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class AlamatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alamat, container, false)

        view.findViewById<View>(R.id.cardUtama).setOnClickListener {
            Toast.makeText(requireContext(), "Alamat utama dipilih", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.cardKedua).setOnClickListener {
            Toast.makeText(requireContext(), "Alamat kedua dipilih", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.cardFavorit).setOnClickListener {
            Toast.makeText(requireContext(), "Pesanan favorit dipilih", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnTambahAlamat).setOnClickListener {
            Toast.makeText(requireContext(), "Tambah alamat baru", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return inflater.inflate(R.layout.fragment_alamat, container, false)
    }
}
