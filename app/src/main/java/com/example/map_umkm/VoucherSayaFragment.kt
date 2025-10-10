package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.VoucherAdapter
import com.example.map_umkm.model.Voucher
import com.example.map_umkm.R

class VoucherSayaFragment : Fragment() {

    private lateinit var rvVoucher: RecyclerView
    private lateinit var adapter: VoucherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_voucher_saya, container, false)
        rvVoucher = view.findViewById(R.id.rvVoucher)

        // FIX: Pass all required arguments to the Voucher data class
        val dummyVouchers = listOf(
            Voucher(
                id = "1", // Example ID
                title = "Diskon 20% Minuman Kopi",
                code = "TUKU20",
                description = "Voucher ini berlaku untuk semua minuman kopi.", // Example description
                expiryDate = "31 Okt 2025",
                isUsed = false
            ),
            Voucher(
                id = "2", // Example ID
                title = "Gratis Ongkir GoFood",
                code = "NGOPI",
                description = "Voucher ini berlaku untuk pesanan GoFood.", // Example description
                expiryDate = "15 Nov 2025",
                isUsed = false
            ),
            Voucher(
                id = "3", // Example ID
                title = "Cashback Rp10.000",
                code = "CB10K",
                description = "Voucher ini memberikan cashback 10k.", // Example description
                expiryDate = "30 Nov 2025",
                isUsed = false
            )
        )

        adapter = VoucherAdapter(dummyVouchers) { voucher ->
            Toast.makeText(requireContext(), "Gunakan ${voucher.code}", Toast.LENGTH_SHORT).show()
        }

        rvVoucher.layoutManager = LinearLayoutManager(requireContext())
        rvVoucher.adapter = adapter

        return view
    }
}