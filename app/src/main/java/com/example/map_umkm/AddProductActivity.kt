package com.example.map_umkm

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import com.example.map_umkm.data.JsonHelper // IMPORT HELPER KITA
import com.example.map_umkm.model.MenuItem     // IMPORT MODEL UNTUK JSON
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// HAPUS SEMUA IMPORT FIREBASE
// import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    // Ganti db dengan JsonHelper
    private lateinit var jsonHelper: JsonHelper

    private lateinit var etName: EditText
    private lateinit var etPriceHot: EditText // Kita butuh 2 input harga
    private lateinit var etPriceIced: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etImageUrl: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ganti layout ke versi yang lebih lengkap
        setContentView(R.layout.activity_add_product)

        // Inisialisasi JsonHelper
        jsonHelper = JsonHelper(this)

        // Hubungkan UI dengan variabel
        etName = findViewById(R.id.et_product_name)
        etPriceHot = findViewById(R.id.et_product_price_hot) // Gunakan ID baru dari layout
        etPriceIced = findViewById(R.id.et_product_price_iced) // Gunakan ID baru dari layout
        spinnerCategory = findViewById(R.id.spinner_category)
        etImageUrl = findViewById(R.id.et_image_url)
        etDescription = findViewById(R.id.et_product_description)
        btnSave = findViewById(R.id.btn_save_product)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_add_product)

        toolbar.setNavigationOnClickListener {
            finish() // Fungsi tombol kembali di toolbar
        }

        setupCategorySpinner()

        btnSave.setOnClickListener {
            saveProductToJson() // Panggil fungsi baru kita
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("WHITE-MILK", "BLACK", "NON-COFFEE", "TUKUDAPAN")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    // FUNGSI BARU UNTUK MENYIMPAN KE JSON
    private fun saveProductToJson() {
        val name = etName.text.toString().trim()
        val priceHotStr = etPriceHot.text.toString().trim()
        val priceIcedStr = etPriceIced.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val imageUrl = etImageUrl.text.toString().trim()
        val description = etDescription.text.toString().trim()

        // Validasi input
        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Nama dan Kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // Harga boleh kosong, jika kosong akan menjadi null di JSON
        val priceHot = priceHotStr.toIntOrNull()
        val priceIced = priceIcedStr.toIntOrNull()

        // 1. Baca data menu yang ada saat ini
        val currentMenuData = jsonHelper.getMenuData()
        if (currentMenuData == null) {
            Toast.makeText(this, "Gagal membaca data menu yang ada.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Buat ID baru untuk produk
        // Cari ID tertinggi saat ini, lalu +1. Jika tidak ada menu, mulai dari 1.
        val newId = (currentMenuData.menu.maxOfOrNull { it.id } ?: 0) + 1

        // 3. Buat objek MenuItem baru
        val newMenuItem = MenuItem(
            id = newId,
            name = name,
            category = category,
            description = description,
            image = if (imageUrl.isNotEmpty()) imageUrl else null,
            price_hot = priceHot,
            price_iced = priceIced,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        // 4. Tambahkan produk baru ke daftar menu
        currentMenuData.menu.add(newMenuItem)

        // 5. Simpan kembali seluruh data menu ke file JSON
        jsonHelper.saveMenuData(currentMenuData)

        // 6. Tampilkan pesan sukses dan tutup activity
        Toast.makeText(this, "Produk '${name}' berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
        finish() // Kembali ke AdminActivity
    }
}
