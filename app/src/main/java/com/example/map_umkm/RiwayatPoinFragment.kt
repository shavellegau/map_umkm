package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.PoinHistoryAdapter
import com.example.map_umkm.model.PoinHistory

class RiwayatPoinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_riwayat_poin, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPoin)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val poinList = listOf(
            PoinHistory("Tambah Poin", "+500", "01 Oktober 2025"),
            PoinHistory("Tukar Voucher", "-2000", "05 Oktober 2025"),
            PoinHistory("Bonus Member Gold", "+1000", "10 Oktober 2025")
        )

        recyclerView.adapter = PoinHistoryAdapter(poinList)
        return view
    }
}
