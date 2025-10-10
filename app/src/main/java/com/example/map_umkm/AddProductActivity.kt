package com.example.map_umkm

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.map_umkm.model.Product // Pastikan ini model yang benar
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etName: EditText
    private lateinit var etPrice: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etImageUrl: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        db = FirebaseFirestore.getInstance()

        // Hubungkan UI dengan variabel
        etName = findViewById(R.id.et_product_name)
        etPrice = findViewById(R.id.et_product_price)
        spinnerCategory = findViewById(R.id.spinner_category)
        etImageUrl = findViewById(R.id.et_image_url)
        etDescription = findViewById(R.id.et_product_description)
        btnSave = findViewById(R.id.btn_save_product)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_add_product)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        setupCategorySpinner()

        btnSave.setOnClickListener {
            saveProductToFirestore()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("WHITE-MILK", "BLACK", "NON-COFFEE", "TUKUDAPAN")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun saveProductToFirestore() {
        val name = etName.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val imageUrl = etImageUrl.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Nama, Harga, dan URL Gambar tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toIntOrNull()
        if (price == null) {
            Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // === PERBAIKAN UTAMA DI SINI ===
        // Sesuaikan dengan nama field di model Product.kt Anda
        val newProduct = hashMapOf(
            "name" to name,
            "description" to description,
            "category" to category,
            "image" to imageUrl, // Menggunakan 'image' bukan 'imageUrl'
            "price_hot" to price, // Menggunakan 'price_hot' bukan 'price'
            "price_iced" to 0, // Beri nilai default untuk price_iced
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Cara yang benar untuk timestamp
        )

        // Simpan sebagai HashMap ke koleksi "products" di Firestore
        db.collection("products")
            .add(newProduct)
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                finish() // Tutup activity dan kembali ke AdminActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menambahkan produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
