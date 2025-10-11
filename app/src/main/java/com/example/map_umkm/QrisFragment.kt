package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class QrisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qris, container, false)
        val btnSelesai = view.findViewById<Button>(R.id.btnSelesai)

        btnSelesai.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, PaymentSuccessFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}