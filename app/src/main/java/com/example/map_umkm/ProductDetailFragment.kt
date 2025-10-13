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
// Hapus import FirestoreHelper yang tidak digunakan lagi di sini
// import com.example.map_umkm.helper.FirestoreHelper
import com.example.map_umkm.model.Product
import com.example.map_umkm.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private lateinit var ivImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCategory: TextView
    private lateinit var ivFavorite: ImageView
    private lateinit var tvPrice: TextView
    private lateinit var rgTemperature: RadioGroup
    private lateinit var rbHot: RadioButton
    private lateinit var rbIced: RadioButton
    private lateinit var btnAddToCart: Button

    private val cartViewModel: CartViewModel by activityViewModels()
    private val args: ProductDetailFragmentArgs by navArgs()
    private lateinit var selectedProduct: Product

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)

        // Initialize Views
        ivImage = view.findViewById(R.id.ivImage)
        tvName = view.findViewById(R.id.tvName)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvCategory = view.findViewById(R.id.tvCategory)
        tvPrice = view.findViewById(R.id.tvPrice)
        rgTemperature = view.findViewById(R.id.rgTemperature)
        rbHot = view.findViewById(R.id.rbHot)
        rbIced = view.findViewById(R.id.rbIced)
        btnAddToCart = view.findViewById(R.id.btnAddToCart)
        ivFavorite = view.findViewById(R.id.ivFavorite)

        // Get product from Safe Args
        selectedProduct = args.product

        // Populate UI
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

        // Setup Listeners
        rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            if (!selectedProduct.category.equals("TUKUDAPAN", ignoreCase = true)) {
                updatePriceForSelection(selectedProduct, checkedId)
            }
        }

        btnAddToCart.setOnClickListener {
            addToCart(selectedProduct)
        }

        ivFavorite.setOnClickListener {
            selectedProduct.isFavorite = !selectedProduct.isFavorite
            updateFavoriteIcon(selectedProduct.isFavorite)
            // (Your Firestore logic can be re-added here if needed)
        }

        return view
    }

    private fun updateUiForProduct(product: Product) {
        if (product.category.equals("TUKUDAPAN", ignoreCase = true)) {
            rgTemperature.visibility = View.GONE
            // [FIXED] Tambahkan elvis operator (?: 0) untuk menangani null
            val price = product.price_hot ?: 0
            tvPrice.text = formatCurrency(price)
            return
        }

        rgTemperature.visibility = View.VISIBLE
        // [FIXED] Cek null, bukan cek 0, karena harga bisa null sekarang
        rbIced.visibility = if (product.price_iced == null) View.GONE else View.VISIBLE
        rbHot.visibility = if (product.price_hot == null) View.GONE else View.VISIBLE

        // [FIXED] Tambahkan elvis operator (?: 0) untuk harga awal
        val initialPrice = product.price_hot ?: product.price_iced ?: 0
        tvPrice.text = formatCurrency(initialPrice)
    }

    private fun updatePriceForSelection(product: Product, checkedId: Int) {
        // [FIXED] Tambahkan elvis operator (?: 0) di setiap cabang when
        val newPrice = when (checkedId) {
            R.id.rbIced -> product.price_iced ?: 0
            else -> product.price_hot ?: 0
        }
        tvPrice.text = formatCurrency(newPrice)
    }

    private fun addToCart(product: Product) {
        val selectedType = if (rbIced.isChecked && product.price_iced != null) "iced" else "hot"
        cartViewModel.addToCart(product, selectedType)

        Toast.makeText(requireContext(), "${product.name} ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
        // Kembali ke fragment sebelumnya dengan NavController
        findNavController().popBackStack()
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount.toLong())
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        ivFavorite.setImageResource(icon)
    }
}
