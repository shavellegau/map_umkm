package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AdminProductAdapter
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Product
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminActivity : AppCompatActivity() {

    private lateinit var jsonHelper: JsonHelper
    private lateinit var rvProducts: RecyclerView
    private lateinit var adapter: AdminProductAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        jsonHelper = JsonHelper(this)
        initializeViews()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchProductsFromJson()
    }

    private fun initializeViews() {
        rvProducts = findViewById(R.id.rv_admin_products)
        progressBar = findViewById(R.id.progress_bar_admin)
        emptyView = findViewById(R.id.tv_empty_view)
    }

    private fun setupListeners() {
        val fab: FloatingActionButton = findViewById(R.id.fab_add_product)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)

        fab.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminProductAdapter(
            productList = emptyList(),
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            },
            onEditClick = { product ->
                // Arahkan ke EditProductActivity dengan membawa data produk
                val intent = Intent(this, EditProductActivity::class.java).apply {
                    putExtra("PRODUCT_EXTRA", product)
                }
                startActivity(intent)
            }
        )
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = adapter
    }

    private fun fetchProductsFromJson() {
        showLoading(true)
        val menuData = jsonHelper.getMenuData()

        if (menuData != null) {
            val productList = menuData.menu.map { menuItem ->
                Product(
                    id = menuItem.id.toString(),
                    name = menuItem.name,
                    category = menuItem.category ?: "",
                    description = menuItem.description ?: "",
                    image = menuItem.image ?: "",
                    price_hot = menuItem.price_hot ?: 0,
                    price_iced = menuItem.price_iced ?: 0
                )
            }.sortedBy { it.name }

            adapter.updateData(productList)
            updateEmptyView(productList.isEmpty())
        } else {
            Toast.makeText(this, "Gagal memuat menu dari file JSON.", Toast.LENGTH_SHORT).show()
            updateEmptyView(true)
        }
        showLoading(false)
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Hapus") { _, _ -> deleteProductFromJson(product) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteProductFromJson(product: Product) {
        val currentMenuData = jsonHelper.getMenuData()
        if (currentMenuData != null) {
            val itemRemoved = currentMenuData.menu.removeIf { it.id == product.id.toInt() }

            if (itemRemoved) {
                jsonHelper.saveMenuData(currentMenuData)
                Toast.makeText(this, "'${product.name}' berhasil dihapus.", Toast.LENGTH_SHORT).show()
                fetchProductsFromJson() // Muat ulang data setelah hapus
            } else {
                Toast.makeText(this, "Gagal menemukan produk untuk dihapus.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ -> logout() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logout() {
        getSharedPreferences("USER_SESSION", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
