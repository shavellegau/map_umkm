package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CategoryAdapter
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.model.Category
import com.example.map_umkm.model.Product
import android.widget.TextView

class CartFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvTotal: TextView

    private val cartList = mutableListOf<Product>()

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


        // 🔹 Tambahin listener
        btnTakeAway.setOnClickListener {
            Toast.makeText(requireContext(), "Take Away dipilih", Toast.LENGTH_SHORT).show()
        }
        btnDelivery.setOnClickListener {
            Toast.makeText(requireContext(), "Delivery dipilih", Toast.LENGTH_SHORT).show()
        }

        // Dummy data kategori
        val categories = listOf(
            Category("Special Offers"),
            Category("Combo Hemat"),
            Category("Coffee"),
            Category("Non-Coffee"),
            Category("Bites")
        )

        // Dummy data produk
        val products = listOf(
            Product("Sahabat Latte 10k", "Rp10.000", "Rp21.000", R.drawable.ic_launcher_foreground, "Special Offers"),
            Product("Combo Hemat 20k", "Rp20.000", "Rp45.000", R.drawable.ic_launcher_foreground, "Combo Hemat"),
            Product("Caramel Latte", "Rp12.000", "Rp24.000", R.drawable.ic_launcher_foreground, "Coffee"),
            Product("Hazelnut Latte", "Rp12.000", "Rp24.000", R.drawable.ic_launcher_foreground, "Coffee")
        )

        rvCategory.layoutManager = LinearLayoutManager(requireContext())
        rvCategory.adapter = CategoryAdapter(categories) { selectedCategory ->
            val filtered = products.filter { it.category == selectedCategory.name }
            (rvProducts.adapter as ProductAdapter).updateData(filtered)
        }

        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = ProductAdapter(products) { product ->
            cartList.add(product)
            updateTotal()
            Toast.makeText(requireContext(), "Ditambahkan: ${product.name}", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun updateTotal() {
        val total = cartList.sumOf { it.price.replace("Rp", "").replace(".", "").toInt() }
        tvTotal.text = "Total: Rp$total"
    }
}
