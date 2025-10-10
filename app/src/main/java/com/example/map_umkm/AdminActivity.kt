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
import com.example.map_umkm.model.Product
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject

class AdminActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var rvProducts: RecyclerView
    private lateinit var adapter: AdminProductAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Setup UI
        rvProducts = findViewById(R.id.rv_admin_products)
        progressBar = findViewById(R.id.progress_bar_admin)
        emptyView = findViewById(R.id.tv_empty_view)
        val fab: FloatingActionButton = findViewById(R.id.fab_add_product)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)

        setupRecyclerView()

        // Hubungkan tombol + ke AddProductActivity
        fab.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        // Handle klik menu logout
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

    // Panggil fetchProducts() di onResume agar data selalu terbaru
    override fun onResume() {
        super.onResume()
        fetchProducts()
    }

    private fun setupRecyclerView() {
        adapter = AdminProductAdapter(emptyList()) { product ->
            showDeleteConfirmation(product)
        }
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = adapter
    }

    private fun fetchProducts() {
        showLoading(true)
        db.collection("products")
            .orderBy("name") // Urutkan berdasarkan nama agar konsisten
            .get()
            .addOnSuccessListener { result ->
                val productList = mutableListOf<Product>()
                for (document in result) {
                    val product = document.toObject<Product>().copy(id = document.id)
                    productList.add(product)
                }
                adapter.updateData(productList)
                updateEmptyView(productList.isEmpty())
                showLoading(false)
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Toast.makeText(this, "Gagal memuat produk: ${exception.message}", Toast.LENGTH_SHORT).show()
                updateEmptyView(true)
            }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Hapus") { _, _ -> deleteProductFromFirestore(product) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteProductFromFirestore(product: Product) {
        if (product.id.isBlank()) return
        db.collection("products").document(product.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "'${product.name}' berhasil dihapus.", Toast.LENGTH_SHORT).show()
                fetchProducts() // Muat ulang data setelah hapus
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
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
        auth.signOut()
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
