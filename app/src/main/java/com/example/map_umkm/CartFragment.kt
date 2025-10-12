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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CategoryAdapter
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.data.JsonHelper // IMPORT HELPER KITA
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

    // TAMBAHKAN JsonHelper
    private lateinit var jsonHelper: JsonHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Inisialisasi JsonHelper
        jsonHelper = JsonHelper(requireContext())

        // findViewById sesuai fragment_cart.xml
        rvCategory = view.findViewById(R.id.rv_category)
        rvProducts = view.findViewById(R.id.rv_products)
        tvTotal = view.findViewById(R.id.tv_total)
        etSearch = view.findViewById(R.id.et_search)
        bottomBar = view.findViewById(R.id.layout_bottom)
        btnViewOrder = view.findViewById(R.id.btn_view_order)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)

        setupSearchListener()
        setupBottomBarListener()
        // GANTI: Panggil fungsi baru kita
        loadMenuFromJsonHelper()

        // Observasi perubahan cart -> update total + bottom bar
        cartViewModel.cartList.observe(viewLifecycleOwner) {
            updateTotal()
        }

        return view
    }

    // FUNGSI INI DIUBAH TOTAL
    private fun loadMenuFromJsonHelper() {
        val menuData = jsonHelper.getMenuData() // Gunakan Helper

        if (menuData == null) {
            Toast.makeText(requireContext(), "Gagal memuat menu dari file.", Toast.LENGTH_SHORT).show()
            return
        }

        // Konversi dari List<MenuItem> ke List<Product>
        allProducts = menuData.menu.map { menuItem ->
            Product(
                id = menuItem.id.toString(),
                name = menuItem.name,
                category = menuItem.category ?: "",
                description = menuItem.description ?: "",
                image = menuItem.image ?: "",
                price_hot = menuItem.price_hot ?: 0,
                price_iced = menuItem.price_iced ?: 0,
                isFavorite = false,
                quantity = 0,
                selectedType = "hot"
            )
        }

        productAdapter = ProductAdapter(
            products = allProducts.toMutableList(),
            onProductClick = { product ->
                val detailFragment = ProductDetailFragment.newInstance(product)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onFavoriteToggle = { product, isFav ->
                product.isFavorite = isFav
                // cari posisi produk di adapter untuk di-update
                val index = productAdapter.products.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    productAdapter.notifyItemChanged(index)
                }
                Toast.makeText(
                    requireContext(),
                    "${product.name} ${if (isFav) "ditambah ke favorit" else "dihapus dari favorit"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = productAdapter

        val categories = listOf(
            Category("WHITE-MILK"),
            Category("BLACK"),
            Category("NON-COFFEE"),
            Category("TUKUDAPAN")
        )

        val categoryAdapter = CategoryAdapter(categories) { selected ->
            selectedCategory = selected.name
            filterProducts(selected.name, etSearch.text.toString())
        }
        rvCategory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategory.adapter = categoryAdapter

        if (categories.isNotEmpty()) {
            selectedCategory = categories[0].name
            categoryAdapter.setSelectedPosition(0) // Highlight kategori pertama
            filterProducts(categories[0].name, "")
        }

        updateBottomBarState()
    }


    // === FUNGSI LAINNYA DI BAWAH INI TETAP SAMA (TIDAK PERLU DIUBAH) ===

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
            val paymentFragment = PaymentFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, paymentFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun filterProducts(categoryName: String?, query: String?) {
        if (!::productAdapter.isInitialized) return // Pengaman jika adapter belum dibuat

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

    private fun updateBottomBarState() {
        val currentCart = cartViewModel.cartList.value ?: emptyList()
        if (currentCart.isNotEmpty()) {
            bottomBar.visibility = View.VISIBLE
            val totalQty = currentCart.sumOf { it.quantity }
            btnViewOrder.text = "Lihat Pesanan ($totalQty)"
        } else {
            bottomBar.visibility = View.GONE
        }
    }

    private fun updateTotal() {
        val currentCart = cartViewModel.cartList.value ?: emptyList()
        val subtotal = currentCart.sumOf { item ->
            val price = if (item.selectedType == "hot") item.price_hot else (item.price_iced ?: item.price_hot)
            price.toDouble() * item.quantity
        }
        val tax = subtotal * 0.10
        val total = subtotal + tax
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        currencyFormat.maximumFractionDigits = 0 // Hilangkan desimal

        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
        tvTotal.text = currencyFormat.format(total)

        updateBottomBarState()
    }
}
