package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PengaturanAkunFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pengaturan_akun, container, false)

        val menuMyProfile = view.findViewById<View>(R.id.menuMyProfile)
        val btnBack = view.findViewById<View>(R.id.btnBack)

        
        menuMyProfile.setOnClickListener {
            findNavController().navigate(R.id.action_pengaturanAkunFragment_to_editProfileFragment)
        }

        
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }
}
