package com.example.map_umkm

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.MenuItem
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddProductActivity : AppCompatActivity() {

    private lateinit var jsonHelper: JsonHelper

    private lateinit var etName: EditText
    private lateinit var etPriceHot: EditText
    private lateinit var etPriceIced: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etImageUrl: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    // [BARU] Tambahan untuk UI upload gambar
    private lateinit var ivProductPreview: ImageView
    private var imageUri: Uri? = null

    // [BARU] Launcher untuk meminta izin
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                showImagePickerDialog() // Jika izin diberikan, tampilkan dialog
            } else {
                Toast.makeText(this, "Izin diperlukan untuk memilih gambar", Toast.LENGTH_SHORT).show()
            }
        }

    // [BARU] Launcher untuk mengambil gambar dari galeri
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                etImageUrl.setText(imageUri.toString()) // Isi URL
                Glide.with(this).load(imageUri).into(ivProductPreview) // Tampilkan preview
            }
        }

    // [BARU] Launcher untuk mengambil foto dari kamera
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Untuk kamera, URI sudah kita siapkan sebelumnya
                etImageUrl.setText(imageUri.toString()) // Isi URL
                Glide.with(this).load(imageUri).into(ivProductPreview) // Tampilkan preview
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        jsonHelper = JsonHelper(this)

        // Hubungkan UI
        etName = findViewById(R.id.et_product_name)
        etPriceHot = findViewById(R.id.et_product_price_hot)
        etPriceIced = findViewById(R.id.et_product_price_iced)
        spinnerCategory = findViewById(R.id.spinner_category)
        etImageUrl = findViewById(R.id.et_image_url)
        etDescription = findViewById(R.id.et_product_description)
        btnSave = findViewById(R.id.btn_save_product)

        // [FIXED] Menggunakan ID yang benar sesuai file layout
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_edit_product)

        // [BARU] Hubungkan UI untuk preview gambar
        ivProductPreview = findViewById(R.id.iv_product_preview)
        val tvChangeImage: TextView = findViewById(R.id.tv_change_image)

        toolbar.setNavigationOnClickListener { finish() }
        setupCategorySpinner()

        btnSave.setOnClickListener { saveProductToJson() }

        // [BARU] Tambahkan listener untuk klik pada gambar/teks
        ivProductPreview.setOnClickListener { checkPermissionsAndShowDialog() }
        tvChangeImage.setOnClickListener { checkPermissionsAndShowDialog() }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("WHITE-MILK", "BLACK", "NON-COFFEE", "TUKUDAPAN")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    // [BARU] Fungsi untuk memeriksa izin sebelum menampilkan dialog
    private fun checkPermissionsAndShowDialog() {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
        if (requiredPermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            showImagePickerDialog()
        } else {
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    // [BARU] Fungsi untuk menampilkan dialog pilihan
    private fun showImagePickerDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val optionCamera = dialog.findViewById<LinearLayout>(R.id.option_camera)
        val optionGallery = dialog.findViewById<LinearLayout>(R.id.option_gallery)

        optionCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Di sini Anda bisa menambahkan logika untuk menyimpan file foto jika diperlukan
            cameraLauncher.launch(cameraIntent)
            dialog.dismiss()
        }

        optionGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun saveProductToJson() {
        val name = etName.text.toString().trim()
        // ... (sisa logika save Anda tetap sama)
        // Pastikan etImageUrl sudah terisi oleh URI gambar dari launcher
        val imageUrl = etImageUrl.text.toString().trim()

        if (name.isEmpty() || spinnerCategory.selectedItem == null) {
            Toast.makeText(this, "Nama dan Kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val priceHot = etPriceHot.text.toString().toIntOrNull()
        val priceIced = etPriceIced.text.toString().toIntOrNull()

        val menuData = jsonHelper.getMenuData()
        if (menuData == null) {
            Toast.makeText(this, "Gagal membaca data menu.", Toast.LENGTH_SHORT).show()
            return
        }

        val newId = (menuData.menu.maxOfOrNull { it.id } ?: 0) + 1
        val newMenuItem = MenuItem(
            id = newId,
            name = name,
            category = spinnerCategory.selectedItem.toString(),
            description = etDescription.text.toString().trim(),
            image = if (imageUrl.isNotEmpty()) imageUrl else null,
            price_hot = priceHot,
            price_iced = priceIced,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        menuData.menu.add(newMenuItem)
        jsonHelper.saveMenuData(menuData)

        Toast.makeText(this, "Produk '${name}' berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
