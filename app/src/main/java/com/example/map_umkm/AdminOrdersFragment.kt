package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AdminOrdersAdapter
import com.example.map_umkm.data.JsonHelper
import android.content.Intent
import com.example.map_umkm.OrderDetailActivity

class AdminOrdersFragment : Fragment() {
    private lateinit var jsonHelper: JsonHelper
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)
        jsonHelper = JsonHelper(requireContext())
        rvOrders = view.findViewById(R.id.rv_admin_orders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter(
            emptyList(),
            onItemClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java).apply {
                    // Pastikan model Order Anda Parcelable
                    putExtra("ORDER_DATA", order)
                }
                startActivity(intent)
            },
            onConfirmPaymentClick = { order ->
                jsonHelper.updateOrderStatus(order.orderId, "Menunggu Konfirmasi")
                loadOrders()
            },
            onProsesClick = { order ->
                jsonHelper.updateOrderStatus(order.orderId, "Diproses")
                loadOrders()
                (activity as? AdminActivity)?.setupOrderNotification()
            },
            onSelesaikanClick = { order ->
                jsonHelper.updateOrderStatus(order.orderId, "Selesai")
                loadOrders()
                (activity as? AdminActivity)?.setupOrderNotification()
            }
        )
        rvOrders.layoutManager = LinearLayoutManager(context)
        rvOrders.adapter = adapter
    }

    private fun loadOrders() {
        val allOrders = jsonHelper.getMenuData()?.orders?.sortedByDescending { it.orderDate } ?: emptyList()
        if (allOrders.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvOrders.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvOrders.visibility = View.VISIBLE
            adapter.updateData(allOrders)
        }
    }
}