package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CategoryAdapter
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.model.Category
import com.example.map_umkm.model.MenuResponse
import com.example.map_umkm.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class CartFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var etSearch: EditText
    private lateinit var bottomBar: View
    private lateinit var btnViewOrder: Button // Tombol Lihat Pesanan/Checkout

    private val cartList = mutableListOf<Product>() // Keranjang belanja
    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList() // Semua produk dari JSON
    private var selectedCategory: String? = null // Kategori yang sedang dipilih

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Inisialisasi View
        rvCategory = view.findViewById(R.id.rv_category)
        rvProducts = view.findViewById(R.id.rv_products)
        tvTotal = view.findViewById(R.id.tv_total)
        etSearch = view.findViewById(R.id.et_search)
        bottomBar = view.findViewById(R.id.layout_bottom)
        btnViewOrder = view.findViewById(R.id.btn_view_order)

        setupSearchListener()
        setupBottomBarListener()

        loadMenuFromAssets()

        return view
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter berdasarkan kategori yang sedang dipilih DAN query pencarian
                filterProducts(selectedCategory, s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomBarListener() {
        btnViewOrder.setOnClickListener {
            if (cartList.isNotEmpty()) {
                // TODO: Ganti ini dengan navigasi Fragment atau Intent ke PaymentActivity/CartActivity
                Toast.makeText(requireContext(), "Navigasi ke halaman Pembayaran/Checkout!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBottomBarState() {
        if (cartList.isNotEmpty()) {
            bottomBar.visibility = View.VISIBLE
            val totalItems = cartList.size

            // Perbarui teks tombol dengan jumlah item
            btnViewOrder.text = "Lihat Pesanan (${totalItems})"
        } else {
            bottomBar.visibility = View.GONE
        }
    }


    private fun loadMenuFromAssets() {
        val jsonString: String? = try {
            requireContext().assets.open("menu_items.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            null
        }

        if (jsonString != null) {
            val gson = Gson()
            val listType = object : TypeToken<MenuResponse>() {}.type
            val menuResponse: MenuResponse? = gson.fromJson(jsonString, listType)

            if (menuResponse != null) {
                val menuList = menuResponse.menu ?: emptyList()

                // 🔹 Simpan semua produk dari JSON ke allProducts
                allProducts = menuList.map {
                    Product(
                        id = it.id ?: 0,
                        name = it.name,
                        price = "Rp${it.price_hot?.toString() ?: "0"}",
                        oldPrice = if (it.price_iced != null) "Rp${it.price_iced}" else null,
                        imageRes = R.drawable.ic_launcher_background,
                        category = it.category
                    )
                }

                // 🔹 Setup ProductAdapter
                productAdapter = ProductAdapter(emptyList()) { product ->
                    cartList.add(product)
                    updateTotal() // Panggil updateTotal setiap kali item ditambahkan
                    Toast.makeText(requireContext(), "Ditambahkan: ${product.name}", Toast.LENGTH_SHORT).show()
                }
                rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
                rvProducts.adapter = productAdapter

                // 🔹 Definisikan kategori manual
                val categories = listOf(
                    Category("WHITE-MILK"),
                    Category("BLACK"),
                    Category("NON-COFFEE"),
                    Category("TUKUDAPAN")
                )

                rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rvCategory.adapter = CategoryAdapter(categories) { selected ->
                    selectedCategory = selected.name
                    filterProducts(selected.name, etSearch.text.toString())
                }

                // 🔹 Tampilkan kategori pertama secara default saat aplikasi dibuka
                if (categories.isNotEmpty()) {
                    selectedCategory = categories[0].name
                    filterProducts(categories[0].name, "")
                }

                updateBottomBarState()

            } else {
                Toast.makeText(requireContext(), "Gagal parse JSON", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "File JSON tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterProducts(categoryName: String?, query: String?) {
        val currentQuery = query.orEmpty().trim()

        // Filter Tahap 1: Berdasarkan Kategori
        val filteredByCategory = if (categoryName.isNullOrEmpty()) {
            allProducts
        } else {
            allProducts.filter {
                it.category.equals(categoryName, ignoreCase = true)
            }
        }

        // Filter Tahap 2: Berdasarkan Pencarian (Nama Produk)
        val finalFiltered = if (currentQuery.isEmpty()) {
            filteredByCategory
        } else {
            filteredByCategory.filter {
                it.name.contains(currentQuery, ignoreCase = true)
            }
        }
        productAdapter.updateData(finalFiltered)
    }

    private fun updateTotal() {
        // Hitung total harga (asumsi harga sudah dalam format integer/numeric sebelum ditambahkan 'Rp')
        val total = cartList.sumOf {
            it.price.replace("Rp", "").replace(".", "").toIntOrNull() ?: 0
        }
        // Format Total Harga (Rp60.000)
        tvTotal.text = "Rp${total}.000"
        updateBottomBarState()
    }
}