package com.example.map_umkm

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Product
import com.google.android.material.appbar.MaterialToolbar

class EditProductActivity : AppCompatActivity() {

    private lateinit var jsonHelper: JsonHelper
    private lateinit var etName: EditText
    private lateinit var etPriceHot: EditText
    private lateinit var etPriceIced: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etImageUrl: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    private var currentProduct: Product? = null
    private val categories = listOf("WHITE-MILK", "BLACK", "NON-COFFEE", "TUKUDAPAN")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        jsonHelper = JsonHelper(this)
        initializeViews()

        currentProduct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("PRODUCT_EXTRA", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("PRODUCT_EXTRA")
        }

        if (currentProduct == null) {
            Toast.makeText(this, "Gagal memuat data produk", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        populateData()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.et_product_name)
        etPriceHot = findViewById(R.id.et_product_price_hot)
        etPriceIced = findViewById(R.id.et_product_price_iced)
        spinnerCategory = findViewById(R.id.spinner_category)
        etImageUrl = findViewById(R.id.et_image_url)
        etDescription = findViewById(R.id.et_product_description)
        btnSave = findViewById(R.id.btn_save_product)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_edit_product) // INI BENAR
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupUI() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun populateData() {
        currentProduct?.let { product ->
            etName.setText(product.name)
            etPriceHot.setText(product.price_hot.toString())
            etPriceIced.setText(product.price_iced.toString())
            etImageUrl.setText(product.image)
            etDescription.setText(product.description)

            val categoryPosition = categories.indexOf(product.category)
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition)
            }
        }
    }

    private fun saveChanges() {
        val productId = currentProduct?.id?.toIntOrNull()
        if (productId == null) {
            Toast.makeText(this, "ID Produk tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        val name = etName.text.toString().trim()
        val priceHot = etPriceHot.text.toString().toIntOrNull()
        val priceIced = etPriceIced.text.toString().toIntOrNull()
        val category = spinnerCategory.selectedItem.toString()
        val imageUrl = etImageUrl.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Nama dan Kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val menuData = jsonHelper.getMenuData()
        if (menuData == null) {
            Toast.makeText(this, "Gagal memuat data menu.", Toast.LENGTH_SHORT).show()
            return
        }

        val productToUpdate = menuData.menu.find { it.id == productId }
        if (productToUpdate == null) {
            Toast.makeText(this, "Produk tidak ditemukan untuk diperbarui.", Toast.LENGTH_SHORT).show()
            return
        }

        // Update data produk
        productToUpdate.apply {
            this.name = name
            this.price_hot = priceHot
            this.price_iced = priceIced
            this.category = category
            this.image = if (imageUrl.isNotEmpty()) imageUrl else null
            this.description = description
        }

        jsonHelper.saveMenuData(menuData)
        Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show()
        finish()
    }
}
