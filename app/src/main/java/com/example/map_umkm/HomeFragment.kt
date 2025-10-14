package com.example.map_umkm

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.databinding.FragmentHomeBinding
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import com.example.map_umkm.viewmodel.FavoriteViewModel
import org.json.JSONObject
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val cartViewModel: CartViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

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
        setupListeners()
        observeFavorites()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onProductClick = { product ->
                val action = HomeFragmentDirections.actionNavHomeToProductDetailFragment(product)
                findNavController().navigate(action)
            },
            onFavoriteToggle = { product, isFavorite ->
                if (isFavorite) {
                    favoriteViewModel.addFavorite(product)
                } else {
                    favoriteViewModel.removeFavorite(product)
                }
            },
            onAddToCartClick = { product ->
                val defaultType = if (product.price_hot != null) "hot" else "iced"
                cartViewModel.addToCart(product, defaultType)
                Toast.makeText(requireContext(), "${product.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerMenu.layoutManager = LinearLayoutManager(context)
        binding.recyclerMenu.adapter = productAdapter
    }

    private fun loadProducts() {
        val menuList = loadMenuFromJson()
        productAdapter.updateProducts(menuList)
    }

    private fun observeFavorites() {
        favoriteViewModel.favoriteProducts.observe(viewLifecycleOwner, Observer { favorites ->
            val favoriteIds = favorites.map { it.id }.toSet()
            productAdapter.updateFavorites(favoriteIds)
        })
    }

    private fun loadMenuFromJson(): List<Product> {
        val jsonString = requireContext().assets.open("menu_items.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("menu")

        val list = mutableListOf<Product>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Product(
                    id = obj.getInt("id").toString(),
                    category = obj.getString("category"),
                    name = obj.getString("name"),
                    description = obj.getString("description"),
                    image = obj.getString("image"),
                    price_hot = if (obj.isNull("price_hot")) null else obj.getInt("price_hot"),
                    price_iced = if (obj.isNull("price_iced")) null else obj.getInt("price_iced"),
                    isFavorite = false
                )
            )
        }
        return list
    }

    private fun setupBannerCarousel() { /* boleh dikosongkan sementara */ }

    private fun setupListeners() { /* boleh dikosongkan sementara */ }

    override fun onDestroyView() {
        timer?.cancel()
        timer = null
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        _binding = null
        super.onDestroyView()
    }
}
