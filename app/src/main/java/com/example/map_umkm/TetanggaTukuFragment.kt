package com.example.map_umkm

import android.animation.ObjectAnimator
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.map_umkm.adapter.TierAdapter
import com.example.map_umkm.model.TierModel
import com.example.map_umkm.viewmodel.TetanggaTukuViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TetanggaTukuFragment : Fragment() {

    private lateinit var viewModel: TetanggaTukuViewModel
    private lateinit var btnBack: ImageView
    private lateinit var btnHistory: Button
    private lateinit var tvPoints: TextView
    private lateinit var tvXpStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewPager: ViewPager2
    private lateinit var rewardsContainer: LinearLayout
    private lateinit var imgCurrentTier: ImageView
    private lateinit var imgNextTier: ImageView
    private lateinit var cardPoints: View

    private var userRealTierIndex: Int = 0
    private var userCurrentXp: Int = 0
    private var previewTierIndex: Int = 0

    private val tierList = listOf(
        TierModel("Bronze", R.drawable.ic_tier_bronze),
        TierModel("Silver", R.drawable.ic_tier_silver),
        TierModel("Gold", R.drawable.ic_tier_gold),
        TierModel("Platinum", R.drawable.ic_tier_platinum),
        TierModel("Diamond", R.drawable.ic_tier_diamond)
    )

    private val tierTargetXp = listOf(0, 100, 250, 500, 1000)

    private val staticRewards = listOf(
        mapOf("title" to "Referral Benefit", "desc" to "Diskon 50% Maks. 20k"),
        mapOf("title" to "Level Up Diskon", "desc" to "Diskon 50% Maks. 20k"),
        mapOf("title" to "Birthday Voucher", "desc" to "Diskon 50% Maks. 50k"),
        mapOf("title" to "3x Voucher Bulanan", "desc" to "Diskon 20% Min. Pembelian 70k"),
        mapOf("title" to "10x Diskon Ongkir", "desc" to "Diskon Ongkir 20% Maks. 12k"),
        mapOf("title" to "Prioritas Pelayanan", "desc" to "Antrian khusus & CS Prioritas")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tetangga_tuku, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[TetanggaTukuViewModel::class.java]

        btnBack = view.findViewById(R.id.btn_back)
        btnHistory = view.findViewById(R.id.btn_history)
        tvPoints = view.findViewById(R.id.label_point_value)
        tvXpStatus = view.findViewById(R.id.tv_xp_status)
        progressBar = view.findViewById(R.id.progress_xp)
        viewPager = view.findViewById(R.id.vp_tier_carousel)
        rewardsContainer = view.findViewById(R.id.ll_rewards_container)
        imgCurrentTier = view.findViewById(R.id.img_current_tier_icon)
        imgNextTier = view.findViewById(R.id.img_next_tier_icon)
        cardPoints = view.findViewById(R.id.card_points)

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnHistory.setOnClickListener {
            findNavController().navigate(R.id.action_tetanggaTukuFragment_to_tukuPointFragment)
        }

        setupViewPager()
        observeData()
        setupCheatButton()

        viewModel.loadData()
    }

    private fun setupCheatButton() {
        cardPoints.setOnClickListener {
            addXpToFirebase(100)
        }
        cardPoints.setOnLongClickListener {
            resetXpToZero()
            true
        }
    }

    private fun addXpToFirebase(amount: Int) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentXp = document.getLong("currentXp")?.toInt() ?: 0
                    val newXp = currentXp + amount
                    db.collection("users").document(userId).update("currentXp", newXp)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Cheat: +$amount XP (Total: $newXp)", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun resetXpToZero() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).update("currentXp", 0)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cheat: Reset XP ke 0", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupViewPager() {
        val adapter = TierAdapter(tierList)
        viewPager.adapter = adapter
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                previewTierIndex = position
                updateUIForTier(previewTierIndex)
            }
        })
    }

    private fun observeData() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvPoints.text = user.currentPoints.toString()
                userCurrentXp = user.currentXp
                updateUIForTier(previewTierIndex)
            }
        }

        viewModel.tierInfo.observe(viewLifecycleOwner) { tier ->
            if (tier != null) {
                userRealTierIndex = tier.tierIndex
                if (previewTierIndex == 0 && viewPager.currentItem != userRealTierIndex) {
                    viewPager.setCurrentItem(userRealTierIndex, false)
                    previewTierIndex = userRealTierIndex
                }
                updateUIForTier(previewTierIndex)
            }
        }
    }

    private fun updateUIForTier(viewedIndex: Int) {
        if (!isAdded) return

        progressBar.visibility = View.VISIBLE
        tvXpStatus.textSize = 12f
        tvXpStatus.setTypeface(null, Typeface.NORMAL)
        imgCurrentTier.visibility = View.VISIBLE

        imgCurrentTier.setImageResource(tierList[viewedIndex].imageResId)
        imgCurrentTier.imageTintList = null
        imgCurrentTier.clearColorFilter()

        val nextIndex = viewedIndex + 1
        if (nextIndex < tierList.size) {
            imgNextTier.visibility = View.VISIBLE
            imgNextTier.setImageResource(tierList[nextIndex].imageResId)
            imgNextTier.imageTintList = null
            imgNextTier.clearColorFilter()
            imgNextTier.alpha = 0.5f
        } else {
            imgNextTier.visibility = View.INVISIBLE
        }

        val limitNextXp = tierTargetXp.getOrElse(viewedIndex + 1) { 100 }
        val targetXpForViewedTier = tierTargetXp.getOrElse(viewedIndex) { 0 }

        if (viewedIndex == userRealTierIndex) {
            if (viewedIndex == tierList.size - 1) {
                tvXpStatus.text = "MAX Level"
                progressBar.max = 100
                progressBar.progress = 100
            } else {
                tvXpStatus.text = "$userCurrentXp / $limitNextXp XP"
                progressBar.max = limitNextXp
                val animation = ObjectAnimator.ofInt(progressBar, "progress", 0, userCurrentXp)
                animation.duration = 800
                animation.interpolator = DecelerateInterpolator()
                animation.start()
            }
        } else if (viewedIndex > userRealTierIndex) {
            val xpNeeded = targetXpForViewedTier - userCurrentXp
            tvXpStatus.text = "Kurang $xpNeeded XP lagi untuk menjadi level ini"
            progressBar.visibility = View.INVISIBLE
            tvXpStatus.textSize = 14f
            tvXpStatus.setTypeface(null, Typeface.BOLD)
        } else {
            tvXpStatus.text = "Level ini sudah terlampaui"
            progressBar.max = 100
            progressBar.progress = 100
        }

        renderRewards(viewedIndex)
    }

    private fun renderRewards(viewedIndex: Int) {
        rewardsContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val unlockedCount = when (userRealTierIndex) {
            0 -> 3; 1, 2 -> 4; 3 -> 5; else -> 6
        }

        for ((index, data) in staticRewards.withIndex()) {
            val itemView = inflater.inflate(R.layout.item_reward_membership, rewardsContainer, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvRewardTitle)
            val tvDesc = itemView.findViewById<TextView>(R.id.tvRewardDesc)
            val overlayLock = itemView.findViewById<View>(R.id.viewOverlayLock)
            val ivLock = itemView.findViewById<ImageView>(R.id.ivLockIcon)

            tvTitle.text = data["title"]
            tvDesc.text = data["desc"]

            val isLocked = index >= unlockedCount
            if (isLocked) {
                overlayLock.visibility = View.VISIBLE
                ivLock.visibility = View.VISIBLE
                itemView.setOnClickListener {
                    Toast.makeText(requireContext(), "Reward terkunci! Naikkan level kamu.", Toast.LENGTH_SHORT).show()
                }
            } else {
                overlayLock.visibility = View.GONE
                ivLock.visibility = View.GONE
                itemView.setOnClickListener {
                    Toast.makeText(requireContext(), "Promo: ${data["title"]}", Toast.LENGTH_SHORT).show()
                }
            }
            rewardsContainer.addView(itemView)
        }
    }
}