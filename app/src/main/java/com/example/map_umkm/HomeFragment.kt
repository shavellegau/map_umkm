package com.example.map_umkm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.adapter.BannerAdapter
import com.example.map_umkm.databinding.FragmentHomeBinding
import com.example.map_umkm.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    // Deklarasi untuk mengontrol Timer dan Handler
    private val handler = android.os.Handler()
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
        setupRecyclerView()
        setupBannerCarousel()
        loadProductsFromJson()
        setupListeners()
    }

    private fun setupBannerCarousel() {
        val bannerImages = listOf(
            R.drawable.banner_kopi,
            R.drawable.tuku_banner,
        )

        val bannerAdapter = BannerAdapter(bannerImages)
        binding.bannerViewPager.adapter = bannerAdapter

        var currentPage = 0

        // 1. Inisialisasi Runnable yang Aman
        updateRunnable = Runnable {
            // PENTING: Periksa apakah binding masih ada sebelum mengakses View
            if (bannerImages.isNotEmpty() && _binding != null) {
                currentPage = (currentPage + 1) % bannerImages.size
                binding.bannerViewPager.setCurrentItem(currentPage, true)
            }
        }

        // 2. Inisialisasi dan Penjadwalan Timer
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                // Gunakan handler untuk memposting ke main thread
                handler.post(updateRunnable!!)
            }
        }, 3000, 3000)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = productList,
            onProductClick = { product ->
                val action = HomeFragmentDirections.actionNavHomeToProductDetailFragment(product)
                findNavController().navigate(action)
            },
            onFavoriteToggle = { product, isFavorite ->
                Log.d("HomeFragment", "Status favorit ${product.name} berubah jadi $isFavorite")
            }
        )

        binding.recyclerMenu.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    private fun loadProductsFromJson() {
        try {
            val inputStream = context?.assets?.open("menu.json")
            val reader = InputStreamReader(inputStream)

            val menuListType = object : TypeToken<Map<String, Any>>() {}.type
            val jsonObject: Map<String, Any> = Gson().fromJson(reader, menuListType)
            val menuArray = jsonObject["menu"] as? List<Map<String, Any>>

            val products = mutableListOf<Product>()
            menuArray?.forEach { map ->
                val id = (map["id"] as? Double)?.toInt() ?: 0
                val name = map["name"] as? String ?: ""
                val category = map["category"] as? String ?: ""
                val description = map["description"] as? String ?: ""
                val image = map["image"] as? String ?: ""
                val price_hot = (map["price_hot"] as? Double)?.toInt() ?: 0
                val price_iced = (map["price_iced"] as? Double)?.toInt() ?: 0

                products.add(
                    Product(
                        id = id.toString(),
                        name = name,
                        category = category,
                        description = description,
                        image = image,
                        price_hot = price_hot,
                        price_iced = price_iced
                    )
                )
            }
            productAdapter.updateData(products)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error loading JSON data: ${e.message}")
        }
    }

    private fun setupListeners() {
        binding.btnNotification.setOnClickListener {
            Log.d("HomeFragment", "Tombol Notifikasi diklik")
        }

        binding.btnChangeLocation.setOnClickListener {
            Log.d("HomeFragment", "Tombol Ubah Lokasi diklik")
        }

        binding.takeAwayCard.setOnClickListener {
            val action = HomeFragmentDirections.actionNavHomeToNavCart()
            findNavController().navigate(action)
        }

        binding.deliveryCard.setOnClickListener {
            val action = HomeFragmentDirections.actionNavHomeToNavCart()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        // PENTING: HENTIKAN TIMER SAAT VIEW DIHANCURKAN!
        timer?.cancel()
        timer = null
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null

        super.onDestroyView()
        _binding = null
    }
}
