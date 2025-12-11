package com.example.map_umkm

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.HistoryAdapter
import com.example.map_umkm.databinding.FragmentTukuPointBinding
import com.example.map_umkm.model.HistoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TukuPointFragment : Fragment() {

    private var _binding: FragmentTukuPointBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private val db = FirebaseFirestore.getInstance()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTukuPointBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        loadTotalPoint()
        selectTab(true)
        loadHistoryEarn()
        setTab()
    }

    private fun setTab() {
        binding.tabDidapat.setOnClickListener {
            selectTab(true)
            loadHistoryEarn()
        }
        binding.tabTerpakai.setOnClickListener {
            selectTab(false)
            loadHistoryRedeem()
        }
    }

    private fun selectTab(isEarn: Boolean) {
        if (isEarn) {
            binding.tabDidapat.setBackgroundColor(Color.WHITE)
            binding.tabDidapat.setTextColor(Color.BLACK)

            binding.tabTerpakai.setBackgroundColor(Color.BLACK)
            binding.tabTerpakai.setTextColor(Color.WHITE)
        } else {
            binding.tabTerpakai.setBackgroundColor(Color.WHITE)
            binding.tabTerpakai.setTextColor(Color.BLACK)

            binding.tabDidapat.setBackgroundColor(Color.BLACK)
            binding.tabDidapat.setTextColor(Color.WHITE)
        }
    }

    private fun loadTotalPoint() {
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val pts = snapshot?.getLong("points") ?: 0
                binding.tvTotalPoint.text = pts.toString()
            }
    }

    private fun loadHistoryEarn() {
        db.collection("users").document(uid)
            .collection("histories")
            .whereEqualTo("type", "earn")
            .addSnapshotListener { snap, _ ->
                val list = snap?.toObjects(HistoryModel::class.java) ?: emptyList()
                adapter.submitList(list)
            }
    }

    private fun loadHistoryRedeem() {
        db.collection("users").document(uid)
            .collection("histories")
            .whereEqualTo("type", "redeem")
            .addSnapshotListener { snap, _ ->
                val list = snap?.toObjects(HistoryModel::class.java) ?: emptyList()
                adapter.submitList(list)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
