package com.example.map_umkm

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.map_umkm.helper.FirestoreHelper
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
    private var selectedProduct: Product? = null

    companion object {
        fun newInstance(product: Product): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            val args = Bundle()
            args.putParcelable("product", product)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)

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

        selectedProduct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("product", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("product")
        }

        selectedProduct?.let { product ->
            tvName.text = product.name ?: ""
            tvDescription.text = product.description ?: ""
            tvCategory.text = product.category ?: ""
            Glide.with(this).load(product.image).into(ivImage)

            // initial favorite icon
            updateFavoriteIcon(product.isFavorite)

            // default temperature selection
            rbHot.isChecked = true
            updateUiForProduct(product)

            rgTemperature.setOnCheckedChangeListener { _, checkedId ->
                updatePriceForSelection(product, checkedId)
            }

            btnAddToCart.setOnClickListener {
                addToCart(product)
            }

            ivFavorite.setOnClickListener {
                product.isFavorite = !product.isFavorite
                updateFavoriteIcon(product.isFavorite)

                if (product.isFavorite) {
                    // --- Panggilan yang biasanya sesuai helper (kirim field) ---
                    FirestoreHelper.addToWishlist(
                        productId = product.id ?: "",
                        productName = product.name ?: "",
                        productPrice = (product.price_hot ?: 0).toString(),
                        imageUrl = product.image ?: ""
                    ) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "${product.name} ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menambahkan ke favorit", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // --- Jika FirestoreHelper menerima Product langsung, bisa dipanggil:
                    // FirestoreHelper.addToWishlist(product) { success -> ... }
                } else {
                    // remove by id (umumnya helper menerima id)
                    FirestoreHelper.removeFromWishlist(product.id ?: "") { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "${product.name} dihapus dari favorit", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menghapus dari favorit", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun updateUiForProduct(product: Product) {
        if (product.price_iced == null) {
            rbIced.visibility = View.GONE
        } else {
            rbIced.visibility = View.VISIBLE
        }

        val initialPrice = if (rbHot.isChecked) (product.price_hot ?: 0) else (product.price_iced ?: product.price_hot ?: 0)
        tvPrice.text = formatCurrency(initialPrice)
    }

    private fun updatePriceForSelection(product: Product, checkedId: Int) {
        val newPrice = when (checkedId) {
            R.id.rbIced -> product.price_iced ?: product.price_hot ?: 0
            else -> product.price_hot ?: 0
        }
        tvPrice.text = formatCurrency(newPrice)
    }

    private fun addToCart(product: Product) {
        val finalPrice = if (rbHot.isChecked) (product.price_hot ?: 0) else (product.price_iced ?: product.price_hot ?: 0)
        val productToAdd = product.copy(price_hot = finalPrice, quantity = 1)
        cartViewModel.addProduct(productToAdd)
        Toast.makeText(requireContext(), "${product.name} added to cart!", Toast.LENGTH_SHORT).show()

        // Kembalikan ke layar sebelumnya (lebih aman daripada replace dengan 'fragment' yang tidak ada)
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount)
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        ivFavorite.setImageResource(icon)
    }
}
