package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.map_umkm.R
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.Locale

class AdminProductAdapter(
    private var productList: List<Product>,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder>() {

    inner class AdminProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.iv_product)
        val productName: TextView = itemView.findViewById(R.id.tv_product_name)
        val productPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductViewHolder {
        // Pastikan Anda sudah membuat layout 'item_admin_product.xml'
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_product, parent, false)
        return AdminProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name

        // Format harga ke Rupiah
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.productPrice.text = format.format(product.price_hot) // Gunakan price_hot sebagai default

        // Muat gambar produk menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(product.image) // 'image' sesuai model Product.kt Anda
            .placeholder(R.drawable.logo_tuku) // Ganti dengan placeholder Anda
            .error(R.drawable.logo_tuku) // Ganti dengan gambar error Anda
            .into(holder.productImage)

        // Atur listener untuk tombol hapus
        holder.deleteButton.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    // Fungsi ini yang dicari oleh AdminActivity.kt
    fun updateData(newProductList: List<Product>) {
        this.productList = newProductList
        notifyDataSetChanged()
    }
}
