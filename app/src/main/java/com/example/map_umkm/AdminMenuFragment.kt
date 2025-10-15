package com.example.map_umkm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AdminProductAdapter
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Product

class AdminMenuFragment : Fragment() {
    private lateinit var jsonHelper: JsonHelper
    private lateinit var rvProducts: RecyclerView
    private lateinit var adapter: AdminProductAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_menu, container, false)
        jsonHelper = JsonHelper(requireContext())
        initializeViews(view)
        setupRecyclerView()
        return view
    }

    private fun initializeViews(view: View) {
        rvProducts = view.findViewById(R.id.rv_admin_products)
        progressBar = view.findViewById(R.id.progress_bar_admin)
        emptyView = view.findViewById(R.id.tv_empty_view)
    }

    override fun onResume() {
        super.onResume()
        fetchProductsFromJson()
    }

    private fun setupRecyclerView() {
        adapter = AdminProductAdapter(
            productList = emptyList(),
            onDeleteClick = { product -> showDeleteConfirmation(product) },
            onEditClick = { product ->
                // [FIXED] Jangan kirim seluruh objek. Kirim ID-nya saja.
                val intent = Intent(activity, EditProductActivity::class.java).apply {
                    putExtra("PRODUCT_ID", product.id.toIntOrNull()) // Kirim ID sebagai Int
                }
                startActivity(intent)
            }
        )
        rvProducts.layoutManager = LinearLayoutManager(context)
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
            Toast.makeText(context, "Gagal memuat menu.", Toast.LENGTH_SHORT).show()
            updateEmptyView(true)
        }
        showLoading(false)
    }

    private fun showDeleteConfirmation(product: Product) {
        // Inflate layout kustom
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null)

        // Buat dialog menggunakan AlertDialog.Builder
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        tvMessage.text = "Yakin ingin menghapus '${product.name}'?"

        // Atur listener untuk tombol
        btnCancel.setOnClickListener {
            dialog.dismiss() // Tutup dialog
        }
        btnDelete.setOnClickListener {
            deleteProductFromJson(product) // Jalankan fungsi hapus
            dialog.dismiss() // Tutup dialog
        }

        // Atur agar background dialog transparan (karena kita pakai CardView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Tampilkan dialog
        dialog.show()
    }



    private fun deleteProductFromJson(product: Product) {
        val currentMenuData = jsonHelper.getMenuData()
        if (currentMenuData != null) {
            val itemRemoved = currentMenuData.menu.removeIf { it.id == product.id.toIntOrNull() }
            if (itemRemoved) {
                jsonHelper.saveMenuData(currentMenuData)
                Toast.makeText(context, "'${product.name}' berhasil dihapus.", Toast.LENGTH_SHORT).show()
                fetchProductsFromJson()
            }
        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
