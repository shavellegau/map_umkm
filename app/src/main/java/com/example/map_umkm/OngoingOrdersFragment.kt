package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.UserOrdersAdapter
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Order

class OngoingOrdersFragment : Fragment(), UserOrdersAdapter.OnItemClickListener {

    private lateinit var jsonHelper: JsonHelper
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: UserOrdersAdapter
    private lateinit var btnBack: ImageView
    private lateinit var tvHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ongoing_orders, container, false)
        jsonHelper = JsonHelper(requireContext())

        // Inisialisasi view
        rvOrders = view.findViewById(R.id.rv_ongoing_orders)
        tvEmpty = view.findViewById(R.id.tv_empty_ongoing)
//    btnBack = view.findViewById(R.id.btnBack)
        tvHeader = view.findViewById(R.id.tvHeader)

        setupRecyclerView()
//    setupListeners()

        return view
    }


    override fun onResume() {
        super.onResume()
        loadOngoingOrders()
    }

    private fun setupRecyclerView() {
        adapter = UserOrdersAdapter(emptyList(), this)
        rvOrders.layoutManager = LinearLayoutManager(context)
        rvOrders.adapter = adapter
    }

//    private fun setupListeners() {
//        // Tombol back kembali ke fragment sebelumnya
//        btnBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//         (Opsional) kalau mau ubah judul dinamis
//        tvHeader.text = "Pesanan Saya"
//    }

    private fun loadOngoingOrders() {
        val userEmail = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getString("userEmail", null)

        if (userEmail == null) {
            showEmptyView(true)
            return
        }

        val allOrders = jsonHelper.getMenuData()?.orders ?: emptyList()
        val ongoingStatuses = listOf("Menunggu Pembayaran", "Menunggu Konfirmasi", "Diproses")

        val myOngoingOrders = allOrders.filter {
            it.userEmail == userEmail && it.status in ongoingStatuses
        }

        if (myOngoingOrders.isEmpty()) {
            showEmptyView(true)
        } else {
            showEmptyView(false)
            adapter.updateData(myOngoingOrders.sortedByDescending { it.orderDate })
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
