package com.example.map_umkm

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
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var etSearch: EditText
    private lateinit var bottomBar: View
    private lateinit var btnViewOrder: Button

    private val cartList = mutableListOf<Product>()
    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        rvCategory = view.findViewById(R.id.rv_category)
        rvProducts = view.findViewById(R.id.rv_products)
        tvTotal = view.findViewById(R.id.tv_total)
        etSearch = view.findViewById(R.id.et_search)
        bottomBar = view.findViewById(R.id.layout_bottom)
        btnViewOrder = view.findViewById(R.id.btn_view_order)

        setupSearchListener()
        setupBottomBarListener()
        loadMenuFromAssets()
        updateTotal() // Tambahkan ini agar total awal 0

        return view
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(selectedCategory, s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomBarListener() {
        btnViewOrder.setOnClickListener {
            val paymentFragment = PaymentFragment.newInstance(ArrayList(cartList))
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, paymentFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateBottomBarState() {
        if (cartList.isNotEmpty()) {
            bottomBar.visibility = View.VISIBLE
            btnViewOrder.text = "Lihat Pesanan (${cartList.sumOf { it.quantity }})"
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

                allProducts = menuList.map {
                    Product(
                        id = it.id ?: 0,
                        name = it.name,
                        price = it.price_hot ?: 0,
                        imageRes = R.drawable.ic_launcher_background,
                        category = it.category
                    )
                }

                productAdapter = ProductAdapter(allProducts.toMutableList()) { product ->
                    val existingProduct = cartList.find { it.id == product.id }
                    if (existingProduct != null) {
                        existingProduct.quantity++
                    } else {
                        cartList.add(product.copy(quantity = 1))
                    }
                    updateTotal()
                    Toast.makeText(requireContext(), "Ditambahkan: ${product.name}", Toast.LENGTH_SHORT).show()
                }

                rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
                rvProducts.adapter = productAdapter

                val categories = listOf(
                    Category("WHITE-MILK"),
                    Category("BLACK"),
                    Category("NON-COFFEE"),
                    Category("TUKUDAPAN")
                )

                rvCategory.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rvCategory.adapter = CategoryAdapter(categories) { selected ->
                    selectedCategory = selected.name
                    filterProducts(selected.name, etSearch.text.toString())
                }

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
        val filteredByCategory = if (categoryName.isNullOrEmpty()) {
            allProducts
        } else {
            allProducts.filter { it.category.equals(categoryName, ignoreCase = true) }
        }

        val finalFiltered = if (currentQuery.isEmpty()) {
            filteredByCategory
        } else {
            filteredByCategory.filter { it.name.contains(currentQuery, ignoreCase = true) }
        }
        productAdapter.updateData(finalFiltered)
    }

    private fun updateTotal() {
        val subtotal = cartList.sumOf { it.price * it.quantity }
        val formattedSubtotal = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(subtotal)
        tvTotal.text = "Subtotal: $formattedSubtotal"
        updateBottomBarState()
    }
}