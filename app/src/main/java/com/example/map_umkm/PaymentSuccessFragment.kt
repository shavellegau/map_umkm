package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.ImageView

class PaymentSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_success, container, false)

        val btnBackToHome = view.findViewById<Button>(R.id.btnBackToHome)

        btnBackToHome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment()) // ganti dengan fragment menu utama kamu
                .commit()
        }

        return view
    }
}
