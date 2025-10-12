package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class QrisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qris, container, false)
        val btnSelesai = view.findViewById<Button>(R.id.btnSelesai)

        // ✅ navigasi pakai action di nav_graph
        btnSelesai.setOnClickListener {
            findNavController().navigate(R.id.action_qrisFragment_to_paymentSuccessFragment)
        }

        return view
    }
}
