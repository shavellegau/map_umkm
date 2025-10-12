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
import com.example.map_umkm.model.Category
import com.example.map_umkm.model.MenuResponse
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // findViewById sesuai fragment_cart.xml
        rvCategory = view.findViewById(R.id.rv_category)
        rvProducts = view.findViewById(R.id.rv_products)
        tvTotal = view.findViewById(R.id.tv_total)
        etSearch = view.findViewById(R.id.et_search)
        bottomBar = view.findViewById(R.id.layout_bottom)
        btnViewOrder = view.findViewById(R.id.btn_view_order)

        // Pastikan id berikut ada pada fragment_cart.xml
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)

        setupSearchListener()
        setupBottomBarListener()
        loadMenuFromAssets()

        // Observasi perubahan cart -> update total + bottom bar
        cartViewModel.cartList.observe(viewLifecycleOwner) {
            updateTotal()
        }

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
            // PERBAIKAN PENTING:
            // Menggunakan findNavController() untuk navigasi yang benar
            findNavController().navigate(R.id.action_nav_cart_to_paymentFragment)
        }
    }

    private fun loadMenuFromAssets() {
        val jsonString: String? = try {
            requireContext().assets.open("menu_items.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            null
        }

        if (jsonString == null) {
            Toast.makeText(requireContext(), "File JSON tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val gson = Gson()
        val listType = object : TypeToken<MenuResponse>() {}.type
        val menuResponse: MenuResponse? = gson.fromJson(jsonString, listType)

        if (menuResponse == null) {
            Toast.makeText(requireContext(), "Gagal parse JSON", Toast.LENGTH_SHORT).show()
            return
        }

        val menuList = menuResponse.menu ?: emptyList()
        allProducts = menuList.map { menuItem ->
            // Sesuaikan tipe price sesuai Product.kt (di projectmu price_hot adalah Int)
            Product(
                id = menuItem.id?.toString() ?: "",
                name = menuItem.name,
                category = menuItem.category ?: "",
                description = menuItem.description ?: "",
                image = menuItem.image ?: "",
                price_hot = menuItem.price_hot ?: 0,
                price_iced = menuItem.price_iced ?: 0,
                isFavorite = false,
                quantity = 0,
                selectedType = "hot" // pastikan properti ini ada di model Product
            )
        }

        productAdapter = ProductAdapter(
            products = allProducts.toMutableList(),
            onProductClick = { product ->
                // PERBAIKAN PENTING:
                // Menggunakan findNavController() untuk navigasi yang benar
                val action = CartFragmentDirections.actionNavCartToProductDetailFragment(product)
                findNavController().navigate(action)
            },
            onFavoriteToggle = { product, isFav ->
                // contoh: toggle di memory + notify
                product.isFavorite = isFav
                productAdapter.notifyDataSetChanged()
                Toast.makeText(
                    requireContext(),
                    "${product.name} ${if (isFav) "ditambah ke favorit" else "dihapus dari favorit"}",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: simpan ke Firestore bila ingin persistent
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

    // FINALLY: updateTotal ada di sini (dalam class CartFragment)
    private fun updateTotal() {
        val currentCart = cartViewModel.cartList.value ?: emptyList()

        // subtotal sebagai Double (meskipun price_hot Int), untuk kalkulasi pajak
        val subtotal = currentCart.sumOf { item ->
            val price = if (item.selectedType == "hot") item.price_hot else (item.price_iced ?: 0)
            price.toDouble() * item.quantity
        }

        val tax = subtotal * 0.10
        val total = subtotal + tax

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
        tvTotal.text = currencyFormat.format(total)

        updateBottomBarState()
    }
}