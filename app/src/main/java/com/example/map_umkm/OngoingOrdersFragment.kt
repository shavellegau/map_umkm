package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.UserOrdersAdapter
import com.example.map_umkm.model.Order
import com.example.map_umkm.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OngoingOrdersFragment : Fragment(), UserOrdersAdapter.OnItemClickListener {

    
    private val TAG = "OngoingOrdersFragment"

    private lateinit var db: FirebaseFirestore
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: UserOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ongoing_orders, container, false)

        
        db = FirebaseFirestore.getInstance()

        
        rvOrders = view.findViewById(R.id.rv_ongoing_orders)
        tvEmpty = view.findViewById(R.id.tv_empty_ongoing)

        setupRecyclerView()

        return view
    }


    override fun onResume() {
        super.onResume()
        
        loadOngoingOrdersFromFirestore()
    }

    private fun setupRecyclerView() {
        adapter = UserOrdersAdapter(emptyList(), this)
        rvOrders.layoutManager = LinearLayoutManager(context)
        rvOrders.adapter = adapter
    }

    private fun loadOngoingOrdersFromFirestore() {
        val userEmail = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getString("userEmail", null)

        if (userEmail == null) {
            showEmptyView(true)
            return
        }

        
        
        val ongoingStatuses = listOf("Menunggu Pembayaran", "Menunggu Konfirmasi", "Diproses", "Dibuat")

        
        db.collection("orders")
            .whereEqualTo("userEmail", userEmail)
            .whereIn("status", ongoingStatuses)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Error mendengarkan pesanan: ", e)
                    
                    showEmptyView(true)
                    Toast.makeText(context, "Gagal memuat data real-time.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val myOngoingOrders = mutableListOf<Order>()
                    for (document in snapshots.documents) {
                        try {
                            
                            val order = document.toObject(Order::class.java)
                            
                            order?.let { myOngoingOrders.add(it) }
                        } catch (e: Exception) {
                            
                            Log.e(TAG, "Gagal memetakan dokumen ID ${document.id} ke Order: ${e.message}")
                        }
                    }

                    if (myOngoingOrders.isEmpty()) {
                        showEmptyView(true)
                    } else {
                        showEmptyView(false)
                        
                        adapter.updateData(myOngoingOrders)
                    }
                }
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