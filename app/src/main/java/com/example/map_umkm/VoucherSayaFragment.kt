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

        val dummyVouchers = listOf(
            Voucher("Diskon 20% Minuman Kopi", "TUKU20", "31 Okt 2025"),
            Voucher("Gratis Ongkir GoFood", "NGOPI", "15 Nov 2025"),
            Voucher("Cashback Rp10.000", "CB10K", "30 Nov 2025")
        )

        adapter = VoucherAdapter(dummyVouchers) { voucher ->
            Toast.makeText(requireContext(), "Gunakan ${voucher.code}", Toast.LENGTH_SHORT).show()
        }

        rvVoucher.layoutManager = LinearLayoutManager(requireContext())
        rvVoucher.adapter = adapter

        return view
    }
}
