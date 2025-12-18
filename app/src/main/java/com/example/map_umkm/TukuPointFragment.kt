package com.example.map_umkm

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.HistoryAdapter
import com.example.map_umkm.databinding.FragmentTukuPointBinding
import com.example.map_umkm.model.History
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.util.Locale

class TukuPointFragment : Fragment() {

    private var _binding: FragmentTukuPointBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userPointsListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null
    private var fullHistoryList: List<History> = emptyList()
    private var currentFilterType: String = "earn"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTukuPointBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()      
        setupTabListeners()   
    }
    override fun onResume() {
        super.onResume()
        loadUserPoints()
        loadPointHistory()
    }
    override fun onPause() {
        super.onPause()
        userPointsListener?.remove()
        historyListener?.remove()
    }
    private fun setupListeners() {
        
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabListeners() {
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.tuku_dark) 
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_bantuan) 
        val activeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_indicator_active)

        
        binding.tabDidapat.setOnClickListener {
            currentFilterType = "earn"
            binding.tabDidapat.background = activeBackground
            binding.tabDidapat.setTextColor(activeTextColor)
            binding.tabDidapat.elevation = 4f
            binding.tabTerpakai.background = null
            binding.tabTerpakai.setTextColor(inactiveTextColor)
            binding.tabTerpakai.elevation = 0f
            filterHistoryList()
        }
        binding.tabTerpakai.setOnClickListener {
            currentFilterType = "redeem"
            binding.tabTerpakai.background = activeBackground
            binding.tabTerpakai.setTextColor(activeTextColor)
            binding.tabTerpakai.elevation = 4f
            binding.tabDidapat.background = null
            binding.tabDidapat.setTextColor(inactiveTextColor)
            binding.tabDidapat.elevation = 0f
            filterHistoryList()
        }
    }

    
    private fun loadUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        userPointsListener?.remove()
        userPointsListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null) {
                    Log.e("TukuPointFragment", "Error loading points", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val points = snapshot.getLong("tukuPoints") ?: 0
                    val formattedPoints = NumberFormat.getNumberInstance(Locale("id", "ID")).format(points)
                    binding.tvTotalPoint.text = formattedPoints
                } else {
                    binding.tvTotalPoint.text = "0"
                }
            }
    }

    
    private fun loadPointHistory() {
        val userId = auth.currentUser?.uid ?: return
        historyListener?.remove()
        historyListener = db.collection("users").document(userId)
            .collection("point_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20) 
            .addSnapshotListener { snapshots, e ->
                if (_binding == null) return@addSnapshotListener

                if (e != null) {
                    Log.e("TukuPointFragment", "Error loading history", e)
                    return@addSnapshotListener
                }

                val tempList = mutableListOf<History>()
                snapshots?.forEach { doc ->
                    val title = doc.getString("title") ?: "Transaksi"
                    val amount = doc.getLong("point") ?: doc.getLong("amount") ?: 0

                    
                    val type = doc.getString("type") ?: "redeem"

                    
                    val imageRes = if (type == "earn") R.drawable.ic_coin else R.drawable.ic_shopping_bag

                    tempList.add(
                        History(
                            title = title,
                            point = amount.toInt(),
                            type = type,
                            imageResId = imageRes
                        )
                    )
                }
                fullHistoryList = tempList
                filterHistoryList()
            }
    }
    private fun filterHistoryList() {
        val filteredList = fullHistoryList.filter { it.type == currentFilterType }
        (binding.rvHistory.adapter as? HistoryAdapter)?.updateData(filteredList)
    }
    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        binding.rvHistory.adapter = HistoryAdapter(mutableListOf())
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}