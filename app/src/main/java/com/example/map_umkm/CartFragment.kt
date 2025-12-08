package com.example.map_umkm

import android.content.Context
import android.content.Intent
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
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import com.example.map_umkm.viewmodel.FavoriteViewModel
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    // UI Components yang sudah ada
    private lateinit var rvCategory: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var etSearch: EditText
    private lateinit var bottomBar: View
    private lateinit var btnViewOrder: Button

    // UI Components BARU untuk Mode & Cabang
    private lateinit var btnTakeAway: Button
    private lateinit var btnDelivery: Button
    private lateinit var tvSelectedBranch: TextView
    private lateinit var btnChangeBranch: TextView

    // ViewModel dan Data
    private val cartViewModel: CartViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private var selectedCategory: String? = null
    private lateinit var jsonHelper: JsonHelper

    // Logic Variables BARU
    private var currentMode: String = "Take Away" // Default mode
    private var selectedBranchName: String? = null

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

        // Panggil setup mode dan cabang di sini
        setupModeAndBranch()

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
        // Muat ulang data cabang setiap kali kembali ke fragment
        loadSelectedBranch()
        // Update mode tampilan (mempertahankan pilihan terakhir)
        setMode(currentMode)
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

        // Inisialisasi View BARU
        btnTakeAway = view.findViewById(R.id.btn_take_away)
        btnDelivery = view.findViewById(R.id.btn_delivery)
        tvSelectedBranch = view.findViewById(R.id.tv_selected_branch)
        btnChangeBranch = view.findViewById(R.id.btn_change_branch)
    }

    // --- FUNGSI BARU UNTUK MODE PENGAMBILAN & CABANG ---
    private fun setupModeAndBranch() {
        // 1. Setup Listeners Mode Pengambilan
        btnTakeAway.setOnClickListener {
            setMode("Take Away")
        }

        btnDelivery.setOnClickListener {
            setMode("Delivery")
        }

        // 2. Setup Listener Tombol Ubah Cabang
        btnChangeBranch.setOnClickListener {
            // Arahkan ke PilihCabangActivity (Asumsi menggunakan startActivity)
            val intent = Intent(requireContext(), PilihCabangActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadSelectedBranch() {
        // Ambil data cabang dari Shared Preferences
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        selectedBranchName = prefs.getString("selectedBranchName", "Cabang Belum Dipilih")

        tvSelectedBranch.text = selectedBranchName

        // Jika belum ada cabang yang dipilih, mungkin perlu beri peringatan
        if (selectedBranchName == "Cabang Belum Dipilih") {
            // Opsional: Tampilkan pesan atau tombol untuk memilih cabang
        }
    }

    private fun setMode(mode: String) {
        currentMode = mode
        val accentColor = ContextCompat.getColor(requireContext(), R.color.tuku_accent)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.tuku_primary)

        // Reset semua tombol ke tampilan default (Delivery/Take Away)
        btnTakeAway.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        btnDelivery.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        btnTakeAway.setTextColor(accentColor)
        btnDelivery.setTextColor(accentColor)

        // Set tombol yang dipilih
        val selectedButton = if (mode == "Take Away") btnTakeAway else btnDelivery
        selectedButton.setBackgroundColor(accentColor)
        selectedButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        // Opsional: Jika mode Delivery dipilih, mungkin ada info biaya tambahan,
        // yang dapat ditampilkan di layout_bottom atau toast.
        if (mode == "Delivery") {
            Toast.makeText(requireContext(), "Mode Delivery dipilih. Biaya kirim akan dihitung saat checkout.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- FUNGSI YANG SUDAH ADA ---

    private fun setupProductAdapter() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onProductClick = { product ->
                val action = CartFragmentDirections
                    .actionNavCartToProductDetailFragment(product)

                // Kirim argumen "source" secara manual tanpa error
                val bundle = action.arguments
                bundle.putString("source", "cart")

                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onFavoriteToggle = { product, isFav ->
                if (isFav) {
                    favoriteViewModel.addFavorite(product)
                    Toast.makeText(requireContext(), "${product.name} ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                } else {
                    favoriteViewModel.removeFavorite(product)
                    Toast.makeText(requireContext(), "${product.name} dihapus dari favorit", Toast.LENGTH_SHORT).show()
                }
                product.isFavorite = isFav
            },
            onAddToCartClick = { product ->
                val action = CartFragmentDirections.actionNavCartToProductDetailFragment(product)
                val bundle = action.arguments
                bundle.putString("source", "cart")
                findNavController().navigate(R.id.productDetailFragment, bundle)
            }
        )

        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = productAdapter
    }

    private fun observeFavorites() {
        favoriteViewModel.favoriteProducts.observe(viewLifecycleOwner) { favorites ->
            val favoriteIds = favorites.map { it.id }.toSet()
            productAdapter.updateFavorites(favoriteIds)
            allProducts = allProducts.map { p -> p.copy(isFavorite = favoriteIds.contains(p.id)) }
        }
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
                price_iced = menuItem.price_iced,
                isFavorite = favoriteViewModel.isFavorite(menuItem.id.toString())
            )
        }
        productAdapter.updateProducts(allProducts)

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
            } else if (selectedBranchName == "Cabang Belum Dipilih" || selectedBranchName.isNullOrEmpty()) {
                Toast.makeText(context, "Mohon pilih cabang terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Simpan currentMode dan selectedBranchName ke ViewModel/Database sebelum navigasi
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
        productAdapter.updateProducts(finalFiltered)
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