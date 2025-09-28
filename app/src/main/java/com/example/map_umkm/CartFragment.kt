package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CategoryAdapter
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvTotal: TextView

    private val cartList = mutableListOf<Product>()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        rvCategory = view.findViewById(R.id.rv_category)
        rvProducts = view.findViewById(R.id.rv_products)
        tvTotal = view.findViewById(R.id.tv_total)

        val btnTakeAway = view.findViewById<Button>(R.id.btn_takeaway)
        val btnDelivery = view.findViewById<Button>(R.id.btn_delivery)

        btnTakeAway.setOnClickListener {
            Toast.makeText(requireContext(), "Take Away dipilih", Toast.LENGTH_SHORT).show()
        }
        btnDelivery.setOnClickListener {
            Toast.makeText(requireContext(), "Delivery dipilih", Toast.LENGTH_SHORT).show()
        }

        // 🔹 Load menu dari API
        loadMenuFromApi()

        return view
    }

    private fun loadMenuFromApi() {
        ApiClient.instance.getMenu().enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val menuList = response.body()?.menu ?: emptyList()

                    // 🔹 Setup ProductAdapter
                    productAdapter = ProductAdapter(menuList.map {
                        Product(
                            name = it.name,
                            price = "Rp${it.price_hot ?: 0}",
                            oldPrice = "Rp${it.price_iced ?: 0}",
                            imageRes = R.drawable.ic_launcher_background, // ✅ kotak hijau
                            category = it.category
                        )
                    }) { product ->
                        cartList.add(product)
                        updateTotal()
                        Toast.makeText(requireContext(), "Ditambahkan: ${product.name}", Toast.LENGTH_SHORT).show()
                    }

                    rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
                    rvProducts.adapter = productAdapter

                    // 🔹 Ambil kategori unik
                    val categories = menuList.map { it.category }.distinct().map { Category(it) }
                    rvCategory.layoutManager = LinearLayoutManager(requireContext())
                    rvCategory.adapter = CategoryAdapter(categories) { selectedCategory ->
                        val filtered = menuList.filter { it.category == selectedCategory.name }
                        productAdapter.updateData(filtered.map {
                            Product(
                                name = it.name,
                                price = "Rp${it.price_hot ?: 0}",
                                oldPrice = "Rp${it.price_iced ?: 0}",
                                imageRes = R.drawable.ic_launcher_background, // ✅ kotak hijau
                                category = it.category
                            )
                        })
                    }

                } else {
                    Toast.makeText(requireContext(), "Gagal load menu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotal() {
        val total = cartList.sumOf {
            it.price.replace("Rp", "").replace(".", "").toIntOrNull() ?: 0
        }
        tvTotal.text = "Total: Rp$total"
    }
}
