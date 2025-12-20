package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
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
import com.example.map_umkm.model.MenuItem
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import com.example.map_umkm.viewmodel.FavoriteViewModel
import com.google.android.material.button.MaterialButton
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

    private lateinit var btnTakeAway: MaterialButton
    private lateinit var btnDelivery: MaterialButton
    private lateinit var toggleSelector: View
    private lateinit var toggleContainer: View

    private lateinit var tvSelectedBranch: TextView
    private lateinit var btnChangeBranch: TextView

    private val cartViewModel: CartViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private var selectedCategory: String? = null
    private lateinit var jsonHelper: JsonHelper

    private var currentMode: String = "Take Away"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        jsonHelper = JsonHelper(requireContext())
        initializeViews(view)

        setupSearchListener()
        setupBottomBarListener()
        setupBranchListener()
        setupProductAdapter()

        setupModeAnimation()
        loadSelectedBranch()

        loadMenu()
        observeFavorites()

        cartViewModel.cartList.observe(viewLifecycleOwner) {
            updateTotal()
        }

        updateTotal()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadSelectedBranch()
        animateMode(currentMode, immediate = true)
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

        btnTakeAway = view.findViewById<MaterialButton>(R.id.btn_take_away)
        btnDelivery = view.findViewById<MaterialButton>(R.id.btn_delivery)
        toggleSelector = view.findViewById(R.id.view_toggle_selector)
        toggleContainer = view.findViewById(R.id.layout_toggle_container)

        tvSelectedBranch = view.findViewById(R.id.tv_selected_branch)
        btnChangeBranch = view.findViewById(R.id.btn_change_branch)
    }

    private fun setupBranchListener() {
        btnChangeBranch.setOnClickListener {
            val intent = Intent(requireContext(), PilihCabangActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupModeAnimation() {
        btnTakeAway.setOnClickListener {
            if (currentMode != "Take Away") {
                currentMode = "Take Away"
                animateMode(currentMode)
            }
        }

        btnDelivery.setOnClickListener {
            if (currentMode != "Delivery") {
                currentMode = "Delivery"
                animateMode(currentMode)
            }
        }
    }

    private fun animateMode(mode: String, immediate: Boolean = false) {
        toggleContainer.post {
            val totalWidth = toggleContainer.width
            val selectorWidth = totalWidth / 2
            toggleSelector.layoutParams.width = selectorWidth
            toggleSelector.requestLayout()

            val targetX = if (mode == "Take Away") 0f else selectorWidth.toFloat()

            if (immediate) {
                toggleSelector.translationX = targetX
            } else {
                toggleSelector.animate()
                    .translationX(targetX)
                    .setDuration(250)
                    .start()
            }

            updateToggleTextStyle(mode)
        }
    }

    private fun updateToggleTextStyle(mode: String) {
        val whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        val darkColor = ContextCompat.getColor(requireContext(), R.color.tuku_dark)

        if (mode == "Take Away") {
            btnTakeAway.setTextColor(whiteColor)
            btnTakeAway.iconTint = ColorStateList.valueOf(whiteColor)

            btnDelivery.setTextColor(darkColor)
            btnDelivery.iconTint = ColorStateList.valueOf(darkColor)

        } else {
            btnDelivery.setTextColor(whiteColor)
            btnDelivery.iconTint = ColorStateList.valueOf(whiteColor)

            btnTakeAway.setTextColor(darkColor)
            btnTakeAway.iconTint = ColorStateList.valueOf(darkColor)
        }
    }

    private fun loadSelectedBranch() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val name = prefs.getString("selectedBranchName", "Cabang Belum Dipilih")
        tvSelectedBranch.text = name
    }

    private fun setupProductAdapter() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onProductClick = { product ->
                navigateToDetail(product)
            },
            onFavoriteToggle = { product, isFav ->
                if (isFav) {
                    favoriteViewModel.addFavorite(product)
                    Toast.makeText(requireContext(), "${product.name} masuk Favorit", Toast.LENGTH_SHORT).show()
                } else {
                    favoriteViewModel.removeFavorite(product)
                    Toast.makeText(requireContext(), "${product.name} dihapus dari Favorit", Toast.LENGTH_SHORT).show()
                }
            },
            onAddToCartClick = { product ->
                navigateToDetail(product)
            }
        )
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = productAdapter
    }

    private fun navigateToDetail(product: Product) {
        try {
            val menuItem = MenuItem(
                id = product.id.toIntOrNull() ?: 0,
                name = product.name,
                description = product.description,
                category = product.category,
                image = product.image,
                createdAt = null,
                price_hot = product.price_hot,
                price_iced = product.price_iced
            )

            val bundle = Bundle()
            bundle.putParcelable("product", menuItem)
            findNavController().navigate(R.id.productDetailFragment, bundle)

        } catch (e: Exception) {
            Toast.makeText(context, "Navigasi Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeFavorites() {
        favoriteViewModel.favoriteProducts.observe(viewLifecycleOwner) { fav ->
            val ids = fav.map { it.id }.toSet()
            productAdapter.updateFavorites(ids)
        }
    }

    private fun loadMenu() {
        val menu = jsonHelper.getMenuData() ?: return

        allProducts = menu.menu.map { m ->
            Product(
                id = m.id.toString(),
                name = m.name,
                category = m.category ?: "Lainnya",
                description = m.description ?: "",
                image = m.image,
                price_hot = m.price_hot,
                price_iced = m.price_iced,
                isFavorite = favoriteViewModel.isFavorite(m.id.toString())
            )
        }
        productAdapter.updateProducts(allProducts)

        val categories = allProducts.map { it.category }.distinct().map { Category(it) }
        rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategory.adapter = CategoryAdapter(categories) { selected ->
            selectedCategory = selected.name
            filterProducts(selectedCategory, etSearch.text.toString())
        }
        if (categories.isNotEmpty()) {
            selectedCategory = categories[0].name
            filterProducts(selectedCategory, "")
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(selectedCategory, s.toString())
            }
        })
    }

    private fun filterProducts(categoryName: String?, query: String?) {
        val q = query.orEmpty().trim()
        val byCategory = if (categoryName.isNullOrEmpty()) allProducts else allProducts.filter { it.category.equals(categoryName, ignoreCase = true) }
        val finalList = if (q.isEmpty()) byCategory else byCategory.filter { it.name.contains(q, ignoreCase = true) }
        productAdapter.updateProducts(finalList)
    }

    private fun setupBottomBarListener() {
        btnViewOrder.setOnClickListener {
            val cart = cartViewModel.cartList.value
            if (cart.isNullOrEmpty()) {
                Toast.makeText(context, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tvSelectedBranch.text == "Cabang Belum Dipilih") {
                Toast.makeText(context, "Mohon pilih cabang terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveOrderPreference()
            findNavController().navigate(R.id.action_nav_cart_to_paymentFragment)
        }
    }

    private fun saveOrderPreference() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("orderType", currentMode)
            .putString("selectedBranchName", tvSelectedBranch.text.toString())
            .apply()
    }

    private fun updateTotal() {
        val cart = cartViewModel.cartList.value ?: emptyList()
        if (cart.isNotEmpty()) {
            bottomBar.visibility = View.VISIBLE
            val qty = cart.sumOf { it.quantity }
            btnViewOrder.text = "Lihat Pesanan ($qty)"
        } else {
            bottomBar.visibility = View.GONE
        }
        val subtotal = cart.sumOf {
            val price = (if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0
            price.toDouble() * it.quantity
        }
        val tax = subtotal * 0.11
        val total = subtotal + tax
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvSubtotal.text = format.format(subtotal)
        tvTax.text = format.format(tax)
        tvTotalPayment.text = format.format(total)
        tvTotal.text = format.format(total)
    }
}