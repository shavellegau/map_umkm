package com.example.map_umkm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.HistoryAdapter
import com.example.map_umkm.databinding.FragmentPointDetailBinding
import com.example.map_umkm.model.History
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PointDetailFragment : Fragment() {

    private var _binding: FragmentPointDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Adapter
        historyAdapter = HistoryAdapter()

        binding.rvPointDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        loadFullHistory()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadFullHistory() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("point_history") // Pastikan nama collection konsisten
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e("PointDetail", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (_binding == null || !isAdded) return@addSnapshotListener

                // Konversi data Firestore ke model History Anda
                val data = snap?.toObjects(History::class.java) ?: emptyList()

                // Update adapter
                historyAdapter.updateData(data)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}