package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.HistoryAdapter
import com.example.map_umkm.databinding.FragmentPointDetailBinding
import com.example.map_umkm.model.History
import com.example.map_umkm.model.HistoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class PointDetailFragment : Fragment() {

    private var _binding: FragmentPointDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = HistoryAdapter() // Init adapter kosong

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
            .collection("histories")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (!isAdded || snap == null) return@addSnapshotListener

                // Ambil data mentah (HistoryModel)
                val rawData = snap.toObjects(HistoryModel::class.java)

                // Konversi ke data UI (History)
                val uiData = rawData.map { model ->
                    // Aman dari null pointer
                    val dateObj = model.timestamp?.toDate()
                    val dateStr = if (dateObj != null) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dateObj)
                    } else {
                        "-"
                    }

                    // Ambil points, jika null pakai point, jika null pakai 0
                    val pointValue = model.points ?: model.point ?: 0

                    History(
                        title = model.title ?: "Transaksi",
                        point = pointValue,
                        imageResId = 0,
                        date = dateStr
                    )
                }

                // Update Adapter
                historyAdapter.updateData(uiData)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}