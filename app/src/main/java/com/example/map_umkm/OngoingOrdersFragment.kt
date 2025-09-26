package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OngoingOrdersFragment : Fragment() {
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

        val dummyOrders = listOf(
            Order("Dine In", "RUKO GREEN LAKE CITY", "Lychee Tea, Salted Egg Chicken Rice", "Rp90.000", "22 Sep 2025 | 12:55"),
            Order("Take Away", "RUKO GREEN LAKE CITY", "Combo Jiwa Toast, French Fries", "Rp129.200", "22 Sep 2025 | 12:52")
        )

        recyclerView.adapter = OrdersAdapter(dummyOrders)
    }
}
