package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
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
    private lateinit var btnBack: ImageButton

    private val cartViewModel: CartViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()
    private val args: ProductDetailFragmentArgs by navArgs()
    private lateinit var selectedProduct: Product

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)

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
        btnBack = view.findViewById(R.id.btnBack)

        selectedProduct = args.product

        // Sinkronkan flag isFavorite dari FavoriteViewModel
        selectedProduct = selectedProduct.copy(isFavorite = favoriteViewModel.isFavorite(selectedProduct.id))

        tvName.text = selectedProduct.name
        tvDescription.text = selectedProduct.description
        tvCategory.text = selectedProduct.category

        Glide.with(this)
            .load(selectedProduct.image)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(ivImage)

        updateFavoriteIcon(selectedProduct.isFavorite)
        rbHot.isChecked = true
        updateUiForProduct(selectedProduct)

        btnBack.setOnClickListener {
            when (args.source) {
                "wishlist" -> findNavController().popBackStack(R.id.wishlistFragment, false)
                "cart" -> findNavController().popBackStack(R.id.nav_cart, false)
                else -> findNavController().navigateUp()
            }
        }

        // observe favorites agar icon update bila diubah di tempat lain
        favoriteViewModel.favoriteProducts.observe(viewLifecycleOwner) { favorites ->
            val isFav = favorites.any { it.id == selectedProduct.id }
            selectedProduct = selectedProduct.copy(isFavorite = isFav)
            updateFavoriteIcon(isFav)
        }

        rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            if (!selectedProduct.category.equals("TUKUDAPAN", ignoreCase = true)) {
                updatePriceForSelection(selectedProduct, checkedId)
            }
        }

        btnAddToCart.setOnClickListener {
            val notes = etNotes.text.toString().trim()
            val wordCount = if (notes.isEmpty()) 0 else notes.split("\\s+".toRegex()).size

            if (wordCount > 50) {
                Toast.makeText(requireContext(), "Catatan maksimal 50 kata!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedType = if (rbHot.isChecked) "hot" else "iced"
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
                Toast.makeText(requireContext(), "${selectedProduct.name} ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
            } else {
                favoriteViewModel.removeFavorite(selectedProduct)
                Toast.makeText(requireContext(), "${selectedProduct.name} dihapus dari favorit", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun updateUiForProduct(product: Product) {
        if (product.category.equals("TUKUDAPAN", ignoreCase = true)) {
            rgTemperature.visibility = View.GONE
            val price = product.price_hot ?: 0
            tvPrice.text = formatCurrency(price)
            return
        }

        rgTemperature.visibility = View.VISIBLE
        rbIced.visibility = if (product.price_iced == null) View.GONE else View.VISIBLE
        rbHot.visibility = if (product.price_hot == null) View.GONE else View.VISIBLE

        val initialPrice = product.price_hot ?: product.price_iced ?: 0
        tvPrice.text = formatCurrency(initialPrice)
    }

    private fun updatePriceForSelection(product: Product, checkedId: Int) {
        val newPrice = when (checkedId) {
            R.id.rbIced -> product.price_iced ?: 0
            else -> product.price_hot ?: 0
        }
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
