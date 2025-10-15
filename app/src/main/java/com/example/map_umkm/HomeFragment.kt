package com.example.map_umkm

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.map_umkm.adapter.BannerAdapter
import com.example.map_umkm.databinding.FragmentHomeBinding
import com.example.map_umkm.model.MenuData
import com.example.map_umkm.model.MenuItem
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var updateRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBannerCarousel()
        loadNewestMenuFromJson()
        setupListeners()
    }

    private fun setupBannerCarousel() {
        val bannerImages = listOf(R.drawable.banner_tuku_hut, R.drawable.tuku_banner, R.drawable.banner_tuku_mrt)
        val bannerAdapter = BannerAdapter(bannerImages)
        binding.bannerViewPager.adapter = bannerAdapter
        var currentPage = 0
        updateRunnable = Runnable {
            if (bannerImages.isNotEmpty() && _binding != null) {
                currentPage = (currentPage + 1) % bannerImages.size
                binding.bannerViewPager.setCurrentItem(currentPage, true)
            }
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                updateRunnable?.let { handler.post(it) }
            }
        }, 3000, 3000)
    }

    private fun loadNewestMenuFromJson() {
        try {
            val inputStream = context?.assets?.open("menu_items.json")
            val reader = InputStreamReader(inputStream)
            val menuDataType = object : TypeToken<MenuData>() {}.type
            val menuData: MenuData = Gson().fromJson(reader, menuDataType)

            if (menuData.menu.isNotEmpty()) {
                val newestMenuItem = menuData.menu.maxByOrNull { it.createdAt ?: "" }

                if (newestMenuItem != null) {
                    binding.newestMenuCard.visibility = View.VISIBLE
                    binding.tvNewestMenuName.text = newestMenuItem.name

                    val priceHot = newestMenuItem.price_hot?.let { "Hot: Rp $it" } ?: ""
                    val priceIced = newestMenuItem.price_iced?.let { "Iced: Rp $it" } ?: ""
                    binding.tvNewestMenuPrice.text = listOf(priceHot, priceIced).filter { it.isNotEmpty() }.joinToString(" / ")

                    newestMenuItem.image?.let {
                        Glide.with(this)
                            .load(Uri.parse(it))
                            .into(binding.ivNewestMenuImage)
                    }

                    binding.newestMenuCard.setOnClickListener {
                        Toast.makeText(requireContext(), "${newestMenuItem.name} diklik!", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    binding.newestMenuCard.visibility = View.GONE
                }
            } else {
                binding.newestMenuCard.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error loading JSON: ${e.message}")
            binding.newestMenuCard.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        // ### PERBAIKAN DI SINI ###
        // Mengakses 'btnNotification' langsung dari 'binding' utama, bukan melalui 'userInfoCard'.
        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_notificationFragment)
        }

        binding.btnTukuPoint.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_tukuPointFragment)
        }

        binding.btnTetanggaTuku.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_tukuPointFragment)
        }


    }

    override fun onDestroyView() {
        timer?.cancel()
        timer = null
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        super.onDestroyView()
        _binding = null
    }
}