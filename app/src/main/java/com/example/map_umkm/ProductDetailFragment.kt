package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.map_umkm.viewmodel.CartViewModel
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private lateinit var ivImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCategory: TextView
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

        selectedProduct = arguments?.getParcelable("product")

        selectedProduct?.let { product ->
            tvName.text = product.name
            tvDescription.text = product.description
            tvCategory.text = product.category
            Glide.with(this).load(product.image).into(ivImage)

            // Update UI based on initial selection and product availability
            updateUiForProduct(product)

            rgTemperature.setOnCheckedChangeListener { _, checkedId ->
                updatePriceForSelection(product, checkedId)
            }

            btnAddToCart.setOnClickListener {
                addToCart(product)
            }
        }

        return view
    }

    // Add this method to handle all UI updates related to the product
    private fun updateUiForProduct(product: Product) {
        // Set the default temperature based on product availability
        if (product.price_iced == null) {
            rbIced.visibility = View.GONE
        } else {
            rbIced.visibility = View.VISIBLE
        }

        // Initial price display
        val initialPrice = if (rbHot.isChecked) product.price_hot else product.price_iced ?: product.price_hot
        tvPrice.text = formatCurrency(initialPrice)
    }

    private fun updatePriceForSelection(product: Product, checkedId: Int) {
        val newPrice = when (checkedId) {
            R.id.rbIced -> product.price_iced ?: product.price_hot
            else -> product.price_hot
        }
        tvPrice.text = formatCurrency(newPrice)
    }

    private fun addToCart(product: Product) {
        val finalPrice = if (rbHot.isChecked) product.price_hot else product.price_iced ?: product.price_hot

        // Create a new product instance with the final price
        val productToAdd = product.copy(price_hot = finalPrice, quantity = 1)

        // Use the ViewModel to add the product
        cartViewModel.addProduct(productToAdd)

        Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun formatCurrency(amount: Int): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount)
    }
}