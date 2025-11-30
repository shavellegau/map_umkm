package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
        inflater: LayoutInflater,
        container: ViewGroup?,
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

        // ➕ Tombol Lihat Semua
        binding.btnLihatSemua.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace((view.parent as ViewGroup).id, PointDetailFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRewardRecycler() {
        val rewards = listOf(
            Reward("Gratis Kopi Susu", 5000, R.drawable.mini_kst),
            Reward("Diskon 20%", 8000, R.drawable.mini_kst),
            Reward("Voucher Makan", 6000, R.drawable.mini_kst),
            Reward("Gratis Donat", 7000, R.drawable.mini_kst),
            Reward("Voucher 10rb", 10000, R.drawable.mini_kst),
            Reward("Diskon Kopi 50%", 12000, R.drawable.mini_kst),
            Reward("Gratis Roti", 4000, R.drawable.mini_kst),
            Reward("Minuman Spesial", 9000, R.drawable.mini_kst),
            Reward("Hadiah Misterius", 15000, R.drawable.mini_kst),
            Reward("Voucher Belanja", 20000, R.drawable.mini_kst)
        )

        rewardAdapter = RewardAdapter(rewards)
        binding.rvRewards.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = rewardAdapter
        }
    }

    private fun setupHistoryRecycler() {
        val histories = listOf(
            History("Tukar Kopi Susu", 5000, R.drawable.mini_kst),
            History("Dapat Voucher Diskon", 8000, R.drawable.mini_kst)
        )

        historyAdapter = HistoryAdapter(histories)
        binding.rvHistory.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
