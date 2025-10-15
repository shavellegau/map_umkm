package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.HistoryAdapter
import com.example.map_umkm.adapter.RewardAdapter
import com.example.map_umkm.databinding.FragmentTukuPointBinding
import com.example.map_umkm.model.History
import com.example.map_umkm.model.Reward

class TukuPointFragment : Fragment() {

    private var _binding: FragmentTukuPointBinding? = null
    private val binding get() = _binding!!

    private lateinit var rewardAdapter: RewardAdapter
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTukuPointBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRewardRecycler()
        setupHistoryRecycler()

        // 🔙 Tombol Back
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // ➕ Tombol lihat semua
        binding.btnLihatSemua.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace((view.parent as ViewGroup).id, PointDetailFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRewardRecycler() {
        val rewards = listOf(
            Reward("Gratis Kopi Susu", 5000, R.drawable.kopi_susu),
            Reward("Diskon 20%", 8000, R.drawable.kopi_susu)
        )

        rewardAdapter = RewardAdapter(rewards)
        binding.rvRewards.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = rewardAdapter
        }
    }

    private fun setupHistoryRecycler() {
        val histories = listOf(
            History("Gratis Kopi Susu", 5000, R.drawable.kopi_susu),
            History("Voucher Diskon", 10000, R.drawable.kopi_susu)
        )

        historyAdapter = HistoryAdapter(histories)
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
