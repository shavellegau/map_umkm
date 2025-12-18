package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.UserOrdersAdapter
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Order
import android.widget.ImageView
import androidx.navigation.fragment.findNavController

class HistoryOrdersFragment : Fragment(), UserOrdersAdapter.OnItemClickListener {

    private lateinit var jsonHelper: JsonHelper
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: UserOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_orders, container, false)
        jsonHelper = JsonHelper(requireContext())
        rvOrders = view.findViewById(R.id.rv_history_orders)
        tvEmpty = view.findViewById(R.id.tv_empty_history)
        setupRecyclerView()

        
        val btnBack = view.findViewById<ImageView>(R.id.btn_back_history)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadHistoryOrders()
    }

    private fun setupRecyclerView() {
        adapter = UserOrdersAdapter(emptyList(), this)
        rvOrders.layoutManager = LinearLayoutManager(context)
        rvOrders.adapter = adapter
    }

    private fun loadHistoryOrders() {
        val userEmail = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getString("userEmail", null)

        if (userEmail == null) {
            showEmptyView(true)
            return
        }

        val allOrders = jsonHelper.getMenuData()?.orders ?: emptyList()
        val myHistoryOrders = allOrders.filter {
            it.userEmail == userEmail && it.status == "Selesai"
        }

        if (myHistoryOrders.isEmpty()) {
            showEmptyView(true)
        } else {
            showEmptyView(false)
            adapter.updateData(myHistoryOrders.sortedByDescending { it.orderDate })
        }
    }

    private fun showEmptyView(isEmpty: Boolean) {
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onItemClick(order: Order) {
        val intent = Intent(requireContext(), OrderDetailActivity::class.java)
        intent.putExtra("ORDER_DATA", order)
        startActivity(intent)
    }
}
