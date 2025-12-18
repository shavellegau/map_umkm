package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.PoinHistoryAdapter 
import com.example.map_umkm.databinding.FragmentPointDetailBinding
import com.example.map_umkm.model.PoinHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class PointDetailFragment : Fragment() {

    private var _binding: FragmentPointDetailBinding? = null
    private val binding get() = _binding!!

    
    private lateinit var historyAdapter: PoinHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFullHistory()

        
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        
        historyAdapter = PoinHistoryAdapter()

        binding.rvPointDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun loadFullHistory() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("histories") 
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Gagal memuat: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val listUI = mutableListOf<PoinHistory>()
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

                    for (document in snapshot.documents) {
                        
                        val title = document.getString("title") ?: "Transaksi"
                        val points = document.getLong("points") ?: 0
                        val type = document.getString("type") ?: "earn" 
                        val timestamp = document.getTimestamp("timestamp")

                        
                        val dateStr = if (timestamp != null) {
                            dateFormat.format(timestamp.toDate())
                        } else {
                            "-"
                        }

                        
                        val amountStr = if (type == "redeem") {
                            "-$points"
                        } else {
                            "+$points"
                        }

                        
                        listUI.add(PoinHistory(title, amountStr, dateStr))
                    }

                    
                    historyAdapter.updateData(listUI)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}