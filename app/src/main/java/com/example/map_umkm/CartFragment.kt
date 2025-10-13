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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CategoryAdapter
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Category
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var rvCategory: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var etSearch: EditText
    private lateinit var bottomBar: View
    private lateinit var btnViewOrder: Button

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private var selectedCategory: String? = null
    private lateinit var jsonHelper: JsonHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        jsonHelper = JsonHelper(requireContext())
        initializeViews(view)
        setupSearchListener()
        setupBottomBarListener()
        setupProductAdapter()
        loadMenu()

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            updateTotal()
        }

        // Panggil updateTotal sekali saat view dibuat
        updateTotal()

        return view
    }

    private fun initializeViews(view: View) {
        rvCategory = view.findViewById(R.id.rv_category)
        rvProducts = view.findViewById(R.id.rv_products)
        tvTotal = view.findViewById(R.id.tv_total)
        etSearch = view.findViewById(R.id.et_search)
        bottomBar = view.findViewById(R.id.layout_bottom)
        btnViewOrder = view.findViewById(R.id.btn_view_order)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)
    }

    private fun setupProductAdapter() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onProductClick = { product ->
                // [FIXED] Gunakan action yang benar dari nav_graph.xml
                val action = CartFragmentDirections.actionNavCartToProductDetailFragment(product)
                findNavController().navigate(action)
            },
            onFavoriteToggle = { product, isFav ->
                product.isFavorite = isFav
                Toast.makeText(requireContext(), "${product.name} ${if (isFav) "ditambah" else "dihapus"} dari favorit", Toast.LENGTH_SHORT).show()
            },
            onAddToCartClick = { product ->
                val defaultType = if (product.price_hot != null) "hot" else "iced"
                cartViewModel.addToCart(product, defaultType)
                Toast.makeText(context, "${product.name} ditambahkan", Toast.LENGTH_SHORT).show()
            }
        )
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = productAdapter
    }

    private fun loadMenu() {
        val menuData = jsonHelper.getMenuData()
        if (menuData == null) {
            Toast.makeText(requireContext(), "Gagal memuat menu.", Toast.LENGTH_SHORT).show()
            return
        }

        allProducts = menuData.menu.map { menuItem ->
            Product(
                id = menuItem.id.toString(),
                name = menuItem.name,
                category = menuItem.category ?: "Lainnya",
                description = menuItem.description ?: "",
                image = menuItem.image,
                price_hot = menuItem.price_hot,
                price_iced = menuItem.price_iced
            )
        }
        productAdapter.updateData(allProducts)

        val categories = allProducts.map { it.category }.distinct().map { Category(it) }
        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategory.adapter = CategoryAdapter(categories) { selected ->
            selectedCategory = selected.name
            (rvCategory.adapter as? CategoryAdapter)?.setSelectedPosition(categories.indexOf(selected))
            filterProducts(selected.name, etSearch.text.toString())
        }
        if (categories.isNotEmpty()) {
            selectedCategory = categories[0].name
            (rvCategory.adapter as? CategoryAdapter)?.setSelectedPosition(0)
            filterProducts(categories[0].name, "")
        }
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
            if (cartViewModel.cartList.value.isNullOrEmpty()) {
                Toast.makeText(context, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            } else {
                // [FIXED] Gunakan action yang benar dari nav_graph.xml
                findNavController().navigate(R.id.action_nav_cart_to_paymentFragment)
            }
        }
    }

    private fun filterProducts(categoryName: String?, query: String?) {
        val currentQuery = query.orEmpty().trim()
        val filteredByCategory = if (categoryName.isNullOrEmpty() || categoryName.equals("Semua", true)) {
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
        val currentCart = cartViewModel.cartList.value ?: emptyList()

        if (currentCart.isNotEmpty()) {
            bottomBar.visibility = View.VISIBLE
            val totalQty = currentCart.sumOf { it.quantity }
            btnViewOrder.text = "Lihat Pesanan ($totalQty)"
        } else {
            bottomBar.visibility = View.GONE
        }

        val subtotal = currentCart.sumOf { item ->
            val price = (if (item.selectedType == "iced") item.price_iced else item.price_hot) ?: 0
            price.toDouble() * item.quantity
        }
        val tax = subtotal * 0.11
        val total = subtotal + tax

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
        tvTotal.text = currencyFormat.format(total)
    }
}
