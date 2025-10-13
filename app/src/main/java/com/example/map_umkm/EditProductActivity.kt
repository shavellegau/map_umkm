package com.example.map_umkm

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Product
import com.google.android.material.appbar.MaterialToolbar
import java.io.File

class EditProductActivity : AppCompatActivity() {

    private lateinit var jsonHelper: JsonHelper
    private lateinit var etName: EditText
    private lateinit var etPriceHot: EditText
    private lateinit var etPriceIced: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etImageUrl: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var ivProductPreview: ImageView

    private var currentProduct: Product? = null
    private var tempImageUri: Uri? = null // URI sementara untuk hasil kamera

    private val categories = listOf("WHITE-MILK", "BLACK", "NON-COFFEE", "TUKUDAPAN")

    // [DISEMPURNAKAN] Kontrak untuk membuka Photo Picker modern.
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                etImageUrl.setText(it.toString())
                Glide.with(this).load(it).into(ivProductPreview)
            }
        }

    // Kontrak untuk mengambil foto dari kamera (tetap sama, sudah modern)
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                tempImageUri?.let {
                    etImageUrl.setText(it.toString())
                    Glide.with(this).load(it).into(ivProductPreview)
                }
            }
        }

    // Kontrak untuk meminta izin (tetap sama)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                showImagePickerDialog()
            } else {
                Toast.makeText(this, "Izin kamera & galeri diperlukan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product) // Pastikan ini benar

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
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_edit_product)
        toolbar.setNavigationOnClickListener { finish() }

        ivProductPreview = findViewById(R.id.iv_product_preview)
        val tvChangeImage: TextView = findViewById(R.id.tv_change_image)

        ivProductPreview.setOnClickListener { checkPermissionsAndShowDialog() }
        tvChangeImage.setOnClickListener { checkPermissionsAndShowDialog() }
    }

    private fun populateData() {
        currentProduct?.let { product ->
            etName.setText(product.name)
            etPriceHot.setText(product.price_hot.toString())
            etPriceIced.setText(product.price_iced?.toString() ?: "")
            etDescription.setText(product.description)

            if (!product.image.isNullOrEmpty()) {
                etImageUrl.setText(product.image)
                Glide.with(this)
                    .load(product.image)
                    .placeholder(R.drawable.bg_category_default)
                    .error(R.drawable.bg_category_default)
                    .into(ivProductPreview)
            }

            val categoryPosition = categories.indexOf(product.category)
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition)
            }
        }
    }

    private fun checkPermissionsAndShowDialog() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        // Untuk Photo Picker modern, kita tidak perlu meminta izin galeri secara eksplisit
        // karena sistem yang menanganinya. Jadi, kita hanya cek izin kamera.
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val optionCamera = dialog.findViewById<LinearLayout>(R.id.option_camera)
        val optionGallery = dialog.findViewById<LinearLayout>(R.id.option_gallery)

        // [FIXED] Menggunakan `let` block untuk memastikan tempImageUri tidak null saat digunakan
        optionCamera.setOnClickListener {
            createImageUri()?.let { uri ->
                tempImageUri = uri
                cameraLauncher.launch(uri)
                dialog.dismiss()
            }
        }

        optionGallery.setOnClickListener {
            // [DISEMPURNAKAN] Memanggil Photo Picker
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun createImageUri(): Uri? {
        val image = File(filesDir, "camera_photo.jpg")
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            image
        )
    }

    // Sisanya tetap sama...
    private fun setupUI() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        btnSave.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        val productId = currentProduct?.id?.toIntOrNull() ?: run {
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

        val menuData = jsonHelper.getMenuData() ?: run {
            Toast.makeText(this, "Gagal memuat data menu.", Toast.LENGTH_SHORT).show()
            return
        }

        val productToUpdate = menuData.menu.find { it.id == productId } ?: run {
            Toast.makeText(this, "Produk tidak ditemukan untuk diperbarui.", Toast.LENGTH_SHORT).show()
            return
        }

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
