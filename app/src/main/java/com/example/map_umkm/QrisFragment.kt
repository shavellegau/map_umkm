package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * [DIROMBAK TOTAL] Fragment ini sekarang hanya untuk menampilkan gambar QRIS.
 * Logika pembuatan pesanan sudah dipindahkan sepenuhnya ke PaymentFragment.
 */
class QrisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qris, container, false)

        val btnSelesai: Button = view.findViewById(R.id.btnSelesai)
        val btnBack: Button = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            // Cukup kembali ke halaman sebelumnya (PaymentFragment)
            findNavController().popBackStack()
        }

        btnSelesai.setOnClickListener {
            // Setelah "membayar", arahkan ke halaman sukses dengan argumen "QRIS".
            // Pesanan sudah dibuat SEBELUM masuk ke halaman ini.
            val action = QrisFragmentDirections.actionQrisFragmentToPaymentSuccessFragment(
                paymentMethod = "QRIS"
            )
            findNavController().navigate(action)
        }

        return view
    }
}
