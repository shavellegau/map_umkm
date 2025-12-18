package com.example.map_umkm

import android.os.Build 
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

import com.bumptech.glide.Glide
import com.example.map_umkm.model.MenuItem 
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import com.example.map_umkm.viewmodel.FavoriteViewModel
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private lateinit var ivImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvPrice: TextView
    private lateinit var ivFavorite: ImageView
    private lateinit var rgTemperature: RadioGroup
    private lateinit var rbHot: RadioButton
    private lateinit var rbIced: RadioButton
    private lateinit var btnAddToCart: Button
    private lateinit var etNotes: EditText
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private val cartViewModel: CartViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

    

    private lateinit var selectedProduct: Product
    private var currentPrice: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)
        initializeViews(view)

        
        

        val menuItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("product", MenuItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("product")
        }

        if (menuItem != null) {
            
            selectedProduct = Product(
                id = menuItem.id.toString(),
                name = menuItem.name,
                description = menuItem.description ?: "",
                category = menuItem.category ?: "Umum",
                price_hot = menuItem.price_hot,
                price_iced = menuItem.price_iced,
                image = menuItem.image ?: "",
                isFavorite = false
            )
        } else {
            
            Toast.makeText(context, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return view
        }
        

        
        selectedProduct = selectedProduct.copy(isFavorite = favoriteViewModel.isFavorite(selectedProduct.id))

        setupUI()
        setupListeners()

        
        favoriteViewModel.favoriteProducts.observe(viewLifecycleOwner) { favorites ->
            val isFav = favorites.any { it.id == selectedProduct.id }
            selectedProduct = selectedProduct.copy(isFavorite = isFav)
            updateFavoriteIcon(isFav)
        }

        return view
    }

    private fun initializeViews(view: View) {
        ivImage = view.findViewById(R.id.ivImage)
        tvName = view.findViewById(R.id.tvName)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvCategory = view.findViewById(R.id.tvCategory)
        tvPrice = view.findViewById(R.id.tvPrice)
        ivFavorite = view.findViewById(R.id.ivFavorite)
        rgTemperature = view.findViewById(R.id.rgTemperature)
        rbHot = view.findViewById(R.id.rbHot)
        rbIced = view.findViewById(R.id.rbIced)
        btnAddToCart = view.findViewById(R.id.btnAddToCart)
        etNotes = view.findViewById(R.id.etNotes)
        toolbar = view.findViewById(R.id.toolbar)
    }

    private fun setupUI() {
        tvName.text = selectedProduct.name
        tvDescription.text = selectedProduct.description
        tvCategory.text = selectedProduct.category

        updateFavoriteIcon(selectedProduct.isFavorite)
        updateUiForProductType(selectedProduct)

        Log.d("PRODUCT_DETAIL", "Memuat gambar dari URL: ${selectedProduct.image}")

        Glide.with(this)
            .load(selectedProduct.image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(ivImage)
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            if (!selectedProduct.category.equals("TUKUDAPAN", ignoreCase = true)) {
                updatePriceForSelection(selectedProduct, checkedId)
            }
        }

        btnAddToCart.setOnClickListener {
            val notes = etNotes.text.toString().trim()
            if (notes.isNotEmpty() && notes.split("\\s+".toRegex()).size > 50) {
                Toast.makeText(requireContext(), "Catatan maksimal 50 kata!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType = if (rgTemperature.visibility == View.VISIBLE && rbIced.isChecked) "iced" else "hot"
            cartViewModel.addToCart(selectedProduct, selectedType, notes)

            Toast.makeText(requireContext(), "${selectedProduct.name} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        ivFavorite.setOnClickListener {
            val newState = !selectedProduct.isFavorite
            selectedProduct = selectedProduct.copy(isFavorite = newState)
            updateFavoriteIcon(newState)

            if (newState) {
                favoriteViewModel.addFavorite(selectedProduct)
                Toast.makeText(requireContext(), "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
            } else {
                favoriteViewModel.removeFavorite(selectedProduct)
                Toast.makeText(requireContext(), "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUiForProductType(product: Product) {
        if (product.category.equals("TUKUDAPAN", ignoreCase = true)) {
            rgTemperature.visibility = View.GONE
            currentPrice = product.price_hot ?: 0
            tvPrice.text = formatCurrency(currentPrice)
            return
        }

        rgTemperature.visibility = View.VISIBLE
        val hasHot = product.price_hot != null && product.price_hot != 0
        val hasIced = product.price_iced != null && product.price_iced != 0

        rbHot.visibility = if (hasHot) View.VISIBLE else View.GONE
        rbIced.visibility = if (hasIced) View.VISIBLE else View.GONE

        if (hasIced) {
            rbIced.isChecked = true
            currentPrice = product.price_iced ?: 0
        } else if (hasHot) {
            rbHot.isChecked = true
            currentPrice = product.price_hot ?: 0
        }

        tvPrice.text = formatCurrency(currentPrice)
    }

    private fun updatePriceForSelection(product: Product, checkedId: Int) {
        val newPrice = when (checkedId) {
            R.id.rbIced -> product.price_iced ?: 0
            else -> product.price_hot ?: 0
        }
        currentPrice = newPrice
        tvPrice.text = formatCurrency(newPrice)
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount.toLong())
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        ivFavorite.setImageResource(icon)
    }
}