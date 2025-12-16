// File: TukuPointFragment.kt (Perbaikan Final & Definitif)
package com.example.map_umkm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.HistoryAdapter
import com.example.map_umkm.databinding.FragmentTukuPointBinding
import com.example.map_umkm.model.History
import com.google.firebase.auth.FirebaseAuth
import com.example.map_umkm.utils.PointService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class TukuPointFragment : Fragment() {


    private var _binding: FragmentTukuPointBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var userPointsListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null

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

    private fun loadUserPoints() {
        val userId = auth.currentUser?.uid ?: return

        userPointsListener?.remove()

        userPointsListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) {
                    Log.w("TukuPointFragment", "Points updated but fragment view is null.")
                    return@addSnapshotListener
                }

                if (e != null) {
                    Log.e("TukuPointFragment", "Error loading points", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val points = snapshot.getLong("tukuPoints") ?: 0
                    // PERBAIKAN FINAL: Menggunakan ID yang benar "tvTotalPoint" dari file XML
                    binding.tvTotalPoint.text = points.toString()
                } else {
                    // PERBAIKAN FINAL: Menggunakan ID yang benar "tvTotalPoint"
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
            .limit(10)
            .addSnapshotListener { snapshots, e ->
                if (_binding == null) {
                    Log.w("TukuPointFragment", "History updated but fragment view is null.")
                    return@addSnapshotListener
                }

                if (e != null) {
                    Log.e("TukuPointFragment", "Error loading history", e)
                    return@addSnapshotListener
                }

                val historyList = mutableListOf<History>()
                snapshots?.forEach { doc ->
                    val title = doc.getString("title") ?: "Transaksi"
                    val amount = doc.getLong("amount") ?: 0
                    val type = doc.getString("type") ?: "redeem"

                    val imageRes = if (type == "earn") R.drawable.ic_earn_point else R.drawable.ic_redeem_point
                    historyList.add(History(title, amount.toInt(), imageRes))
                }
                (binding.rvHistory.adapter as? HistoryAdapter)?.updateData(historyList)
            }
    }

    private fun setupRecyclerView() {
        // Asumsi RecyclerView di layout Anda untuk riwayat poin memiliki ID "rvHistory"
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        binding.rvHistory.adapter = HistoryAdapter(mutableListOf())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
