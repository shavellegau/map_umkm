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

    // Simpan semua data history di sini agar bisa di-filter tanpa request ulang ke database
    private var fullHistoryList: List<History> = emptyList()

    // Status tab aktif saat ini ("earn" atau "redeem")
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
        setupListeners()      // Tombol Back
        setupTabListeners()   // Logika Tab Pindah-pindah
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
        // Tombol Back di Header
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // --- 1. LOGIKA TAB (DIDAPAT vs TERPAKAI) ---
    private fun setupTabListeners() {
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.tuku_dark) // Warna Coklat Tua
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_bantuan) // Warna Abu/Pudar
        val activeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_indicator_active)

        // Klik Tab DIDAPAT
        binding.tabDidapat.setOnClickListener {
            currentFilterType = "earn"

            // Visual Aktif
            binding.tabDidapat.background = activeBackground
            binding.tabDidapat.setTextColor(activeTextColor)
            binding.tabDidapat.elevation = 4f

            // Visual Non-Aktif
            binding.tabTerpakai.background = null
            binding.tabTerpakai.setTextColor(inactiveTextColor)
            binding.tabTerpakai.elevation = 0f

            // Filter List
            filterHistoryList()
        }

        // Klik Tab TERPAKAI
        binding.tabTerpakai.setOnClickListener {
            currentFilterType = "redeem"

            // Visual Aktif
            binding.tabTerpakai.background = activeBackground
            binding.tabTerpakai.setTextColor(activeTextColor)
            binding.tabTerpakai.elevation = 4f

            // Visual Non-Aktif
            binding.tabDidapat.background = null
            binding.tabDidapat.setTextColor(inactiveTextColor)
            binding.tabDidapat.elevation = 0f

            // Filter List
            filterHistoryList()
        }
    }

    // --- 2. LOAD TOTAL POIN ---
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
                    // Pastikan nama field sesuai database ("tukuPoints")
                    val points = snapshot.getLong("tukuPoints") ?: 0

                    // Format Angka (1136 -> 1.136)
                    val formattedPoints = NumberFormat.getNumberInstance(Locale("id", "ID")).format(points)
                    binding.tvTotalPoint.text = formattedPoints
                } else {
                    binding.tvTotalPoint.text = "0"
                }
            }
    }

    // --- 3. LOAD HISTORY DARI FIRESTORE ---
    private fun loadPointHistory() {
        val userId = auth.currentUser?.uid ?: return
        historyListener?.remove()

        historyListener = db.collection("users").document(userId)
            .collection("point_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20) // Ambil 20 transaksi terakhir
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

                    // Pastikan tipe di database adalah "earn" atau "redeem"
                    val type = doc.getString("type") ?: "redeem"

                    // Ikon: Koin untuk Earn, Voucher/Shopping Bag untuk Redeem
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

                // Simpan ke variabel global
                fullHistoryList = tempList

                // Tampilkan sesuai tab yang sedang aktif
                filterHistoryList()
            }
    }

    // --- 4. FILTER LIST LOKAL ---
    private fun filterHistoryList() {
        // Filter data berdasarkan tipe tab yang aktif saat ini
        val filteredList = fullHistoryList.filter { it.type == currentFilterType }

        // Update Adapter
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