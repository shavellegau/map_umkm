package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryOrdersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orders_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // contoh dummy history order
        val dummyHistory = listOf(
            Order("Dine In", "RUKO GREEN LAKE CITY", "Americano, Nasi Goreng", "Rp70.000", "20 Sep 2025 | 14:10"),
            Order("Take Away", "RUKO GREEN LAKE CITY", "Lychee Tea, French Fries", "Rp45.000", "19 Sep 2025 | 11:35")
        )

        recyclerView.adapter = OrdersAdapter(dummyHistory)
    }
}
