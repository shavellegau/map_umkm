package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PaymentMethodFragment : Fragment() {

    private lateinit var btnQRIS: Button
    private lateinit var btnDana: Button
    private lateinit var btnOvo: Button
    private lateinit var btnCash: Button
    private lateinit var btnBack: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_method, container, false)

        btnQRIS = view.findViewById(R.id.btnQRIS)
        btnDana = view.findViewById(R.id.btnDana)
        btnOvo = view.findViewById(R.id.btnOvo)
        btnCash = view.findViewById(R.id.btnCash)
        btnBack = view.findViewById(R.id.btnBack)

        // tombol kembali
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // ✅ Tombol QRIS → pakai action dari nav_graph
        btnQRIS.setOnClickListener {
            findNavController().navigate(R.id.action_paymentMethodFragment_to_qrisFragment)
        }

        return view
    }
}