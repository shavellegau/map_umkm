package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// import androidx.compose.ui.semantics.text  // <-- [FIXED] BARIS INI DIHAPUS
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.map_umkm.databinding.FragmentPaymentSuccessBinding

class PaymentSuccessFragment : Fragment() {

    private var _binding: FragmentPaymentSuccessBinding? = null
    private val binding get() = _binding!!
    private val args: PaymentSuccessFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentSuccessBinding.inflate(inflater, container, false)
        val view = binding.root

        // Kode ini sekarang akan berfungsi karena ID sudah ada di XML
        val paymentMethod = args.paymentMethod
        if (paymentMethod == "CASH") {
            binding.tvSuccessMessage.text = "Pesanan Dibuat!"
            binding.tvSuccessSubtitle.text = "Silakan lakukan pembayaran di kasir untuk diproses lebih lanjut."
        } else {
            // Pesan default untuk QRIS
            binding.tvSuccessMessage.text = "Pembayaran Berhasil!"
            binding.tvSuccessSubtitle.text = "Pesanan Anda sedang diproses."
        }

        binding.btnBackToHome.setOnClickListener {
            findNavController().navigate(R.id.action_paymentSuccessFragment_to_nav_home)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
