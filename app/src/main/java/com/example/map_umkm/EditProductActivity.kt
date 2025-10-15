package com.example.map_umkm

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import com.example.map_umkm.model.MenuItem
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

    private var currentMenuItem: MenuItem? = null
    private var tempImageUri: Uri? = null

    private val categories = listOf("WHITE-MILK", "BLACK", "NON-COFFEE", "TUKUDAPAN")

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                etImageUrl.setText(it.toString())
                Glide.with(this).load(it).into(ivProductPreview)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                tempImageUri?.let {
                    etImageUrl.setText(it.toString())
                    Glide.with(this).load(it).into(ivProductPreview)
                }
            }
        }

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
        setContentView(R.layout.activity_edit_product)

        jsonHelper = JsonHelper(this)

        // [FIXED] Panggil initializeViews() di awal agar semua view siap pakai.
        initializeViews()

        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        if (productId == -1) {
            Toast.makeText(this, "ID Produk tidak valid.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val menuData = jsonHelper.getMenuData()
        currentMenuItem = menuData?.menu?.find { it.id == productId }

        if (currentMenuItem == null) {
            Toast.makeText(this, "Gagal memuat data produk", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // [FIXED] Panggil setupUI() dan populateData() setelah view dan data siap.
        setupUI()
        populateData()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.et_product_name)
        etPriceHot = findViewById(R.id.et_product_price_hot)
        etPriceIced = findViewById(R.id.et_product_price_iced)
        spinnerCategory = findViewById(R.id.spinner_category)
        etImageUrl = findViewById(R.id.et_image_url) // ID ini ada di activity_add_product.xml, pastikan sama
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
        currentMenuItem?.let { product ->
            etName.setText(product.name)
            etPriceHot.setText(product.price_hot?.toString() ?: "")
            etPriceIced.setText(product.price_iced?.toString() ?: "")
            etDescription.setText(product.description ?: "")

            if (!product.image.isNullOrEmpty()) {
                etImageUrl.setText(product.image)
                Glide.with(this)
                    .load(product.image)
                    .placeholder(R.drawable.bg_category_default)
                    .error(R.drawable.bg_category_default)
                    .into(ivProductPreview)
            }

            val categoryPosition = categories.indexOfFirst { it.equals(product.category, ignoreCase = true) }
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition)
            }
        }
    }

    private fun saveChanges() {
        val menuItemToUpdate = currentMenuItem ?: run {
            Toast.makeText(this, "Produk tidak valid.", Toast.LENGTH_SHORT).show()
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

        val productInList = menuData.menu.find { it.id == menuItemToUpdate.id } ?: run {
            Toast.makeText(this, "Produk tidak ditemukan untuk diperbarui.", Toast.LENGTH_SHORT).show()
            return
        }

        productInList.apply {
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

    private fun setupUI() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        btnSave.setOnClickListener { saveChanges() }
    }

    private fun checkPermissionsAndShowDialog() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

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

        optionCamera.setOnClickListener {
            createImageUri()?.let { uri ->
                tempImageUri = uri
                cameraLauncher.launch(uri)
                dialog.dismiss()
            }
        }

        optionGallery.setOnClickListener {
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
}
