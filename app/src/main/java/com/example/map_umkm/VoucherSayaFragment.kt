package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.VoucherAdapter
import com.example.map_umkm.model.Voucher

class VoucherSayaFragment : Fragment() {

    private lateinit var rvVoucher: RecyclerView
    private lateinit var adapter: VoucherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_voucher_saya, container, false)
        rvVoucher = view.findViewById(R.id.rvVoucher)

        // Tombol Back
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack() // Kembali ke fragment sebelumnya
        }

        // Data Dummy Voucher
        val dummyVouchers = listOf(
            Voucher("1", "Diskon 20% Minuman Kopi", "TUKU20", "Berlaku untuk semua minuman kopi.", "31 Okt 2025", false),
            Voucher("2", "Gratis Ongkir GoFood", "NGOPI", "Berlaku untuk pesanan GoFood.", "15 Nov 2025", false),
            Voucher("3", "Cashback Rp10.000", "CB10K", "Memberikan cashback 10k.", "30 Nov 2025", false)
        )

        adapter = VoucherAdapter(dummyVouchers) { voucher ->
            Toast.makeText(requireContext(), "Gunakan ${voucher.code}", Toast.LENGTH_SHORT).show()
        }

        rvVoucher.layoutManager = LinearLayoutManager(requireContext())
        rvVoucher.adapter = adapter

        return view
    }
}
