package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.map_umkm.R
import com.example.map_umkm.databinding.FragmentPointDetailBinding
import com.example.map_umkm.model.Reward

class PointDetailFragment : Fragment() {

    private var _binding: FragmentPointDetailBinding? = null
    private val binding get() = _binding!!

    private val rewards = listOf(
        Reward("Gratis Kopi Susu", 5000, R.drawable.kopi_susu),
        Reward("Diskon 20%", 8000, R.drawable.kopi_susu),
        Reward("Voucher Gratis", 10000, R.drawable.kopi_susu),
        Reward("Paket Spesial", 15000, R.drawable.kopi_susu)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔙 Tombol Back
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        populateRewards()
    }


    private fun populateRewards() {
        val gridLayout: GridLayout = binding.gridRewards
        val inflater = LayoutInflater.from(context)

        rewards.forEach { reward ->
            val itemView = inflater.inflate(R.layout.item_history, gridLayout, false)
            val imageView = itemView.findViewById<ImageView>(R.id.imageView)
            val titleView = itemView.findViewById<TextView>(R.id.title)
            val pointView = itemView.findViewById<TextView>(R.id.point)

            titleView.text = reward.title
            pointView.text = "${reward.point} Poin"
            Glide.with(this).load(reward.imageResId).into(imageView)

            gridLayout.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
