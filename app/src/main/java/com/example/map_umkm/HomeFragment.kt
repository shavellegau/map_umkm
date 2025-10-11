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
import com.example.map_umkm.databinding.FragmentHomeBinding
import com.example.map_umkm.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

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
        loadProductsFromJson()
        setupListeners()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = productList,
            onProductClick = { product ->
                // MENGGANTI NavHomeDirections MENJADI HomeFragmentDirections
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
            // MENGGANTI NavHomeDirections MENJADI HomeFragmentDirections
            val action = HomeFragmentDirections.actionNavHomeToNavCart()
            findNavController().navigate(action)
        }

        binding.deliveryCard.setOnClickListener {
            // MENGGANTI NavHomeDirections MENJADI HomeFragmentDirections
            val action = HomeFragmentDirections.actionNavHomeToNavCart()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}