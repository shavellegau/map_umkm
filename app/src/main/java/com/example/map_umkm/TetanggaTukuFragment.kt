package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.map_umkm.R
import com.example.map_umkm.adapter.TierAdapter
import com.example.map_umkm.model.TierModel
import com.example.map_umkm.viewmodel.TetanggaTukuViewModel

class TetanggaTukuFragment : Fragment() {

    private lateinit var viewModel: TetanggaTukuViewModel

    // UI Components
    private lateinit var tvPoints: TextView
    private lateinit var tvXpStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewPager: ViewPager2
    private lateinit var rewardsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menggunakan layout utama fragment yang baru (pastikan yang ada LinearLayout container-nya)
        return inflater.inflate(R.layout.fragment_tetangga_tuku, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init ViewModel
        viewModel = ViewModelProvider(this)[TetanggaTukuViewModel::class.java]

        // Init Views
        tvPoints = view.findViewById(R.id.label_point_value)
        tvXpStatus = view.findViewById(R.id.tv_xp_status)
        progressBar = view.findViewById(R.id.progress_xp)
        viewPager = view.findViewById(R.id.vp_tier_carousel)
        rewardsContainer = view.findViewById(R.id.ll_rewards_container)

        setupViewPager()
        observeData()

        // Panggil data dari Firebase
        viewModel.loadData()
    }

    private fun setupViewPager() {
        val dataTier = listOf(
            TierModel("Bronze", R.drawable.ic_tier_bronze),
            TierModel("Silver", R.drawable.ic_tier_silver),
            TierModel("Gold", R.drawable.ic_tier_gold),
            TierModel("Platinum", R.drawable.ic_tier_platinum),
            TierModel("Diamond", R.drawable.ic_tier_diamond)
        )
        val adapter = TierAdapter(dataTier)
        viewPager.adapter = adapter
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    private fun observeData() {
        // 1. Observe User Data (Points)
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            tvPoints.text = user.currentPoints.toString()
        }

        // 2. Observe Tier & XP
        viewModel.tierInfo.observe(viewLifecycleOwner) { tier ->
            val currentXp = viewModel.userData.value?.currentXp ?: 0
            tvXpStatus.text = "$currentXp / ${tier.maxXp} XP"
            progressBar.max = tier.maxXp
            progressBar.progress = currentXp
            viewPager.setCurrentItem(tier.tierIndex, true)
        }

        // 3. Observe Rewards (Dynamic List)
        viewModel.rewards.observe(viewLifecycleOwner) { rewardsList ->
            rewardsContainer.removeAllViews() // Bersihkan list lama

            val inflater = LayoutInflater.from(requireContext())

            for (reward in rewardsList) {
                // --- PERBAIKAN DI SINI ---
                // Menggunakan layout: item_reward_membership
                val itemView = inflater.inflate(R.layout.item_reward_membership, rewardsContainer, false)

                // Pastikan ID di dalam item_reward_membership.xml kamu adalah tvRewardTitle & tvRewardDesc
                val tvTitle = itemView.findViewById<TextView>(R.id.tvRewardTitle)
                val tvDesc = itemView.findViewById<TextView>(R.id.tvRewardDesc)

                tvTitle.text = reward.title
                tvDesc.text = "${reward.description} ${reward.discount}"

                rewardsContainer.addView(itemView)
            }
        }
    }
}