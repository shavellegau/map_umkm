package com.example.map_umkm

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.BannerAdapter
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.databinding.FragmentHomeBinding
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
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
        setupRecyclerView()
        setupBannerCarousel()
        loadProductsFromJson()
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
                // Pastikan runnable tidak null saat dijalankan
                updateRunnable?.let { handler.post(it) }
            }
        }, 3000, 3000)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onProductClick = { product ->
                val action = HomeFragmentDirections.actionNavHomeToProductDetailFragment(product)
                findNavController().navigate(action)
            },
            onFavoriteToggle = { product, isFavorite ->
                Log.d("HomeFragment", "Status favorit ${product.name} berubah jadi $isFavorite")
            },
            onAddToCartClick = { product ->
                val defaultType = if (product.price_hot != null) "hot" else "iced"
                cartViewModel.addToCart(product, defaultType)
                Toast.makeText(requireContext(), "${product.name} ditambahkan", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerMenu.layoutManager = LinearLayoutManager(context)
        binding.recyclerMenu.adapter = productAdapter
        binding.recyclerMenu.isNestedScrollingEnabled = false
    }

    private fun loadProductsFromJson() {
        try {
            val inputStream = context?.assets?.open("menu_items.json")
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
                val image = map["image"] as? String
                val price_hot = (map["price_hot"] as? Double)?.toInt()
                val price_iced = (map["price_iced"] as? Double)?.toInt()
                products.add(Product(id.toString(), name, category, description, image, price_hot, price_iced))
            }
            productAdapter.updateData(products)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error loading JSON: ${e.message}")
        }
    }

    private fun setupListeners() {
        binding.btnNotification.setOnClickListener {
            Log.d("HomeFragment", "Tombol Notifikasi diklik")
        }

        // [DIHAPUS] Listener untuk bottom bar dihapus karena bottom bar tidak ada lagi di sini.
    }

    // [DIHAPUS] Fungsi updateBottomBar dihapus.

    override fun onDestroyView() {
        timer?.cancel()
        timer = null
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        super.onDestroyView()
        _binding = null
    }
}
