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
    private val onDeleteClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder>() {

    inner class AdminProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Pastikan ID ini SAMA PERSIS dengan di file item_admin_product.xml
        val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val productName: TextView = itemView.findViewById(R.id.tvProductName)
        val productPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
        val editButton: ImageButton = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_product, parent, false)
        return AdminProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name

        // Format harga ke Rupiah
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val priceToShow = product.price_hot ?: product.price_iced ?: 0
        holder.productPrice.text = format.format(priceToShow)

        // --- PERBAIKAN GLIDE (Load URL Langsung) ---
        // Hapus kode getIdentifier, langsung load String URL dari model
        Glide.with(holder.itemView.context)
            .load(product.image) // Ini berisi "https://..."
            .placeholder(R.drawable.logo_tuku) // Gambar loading
            .error(R.drawable.error_image)     // Gambar jika URL rusak/kosong
            .centerCrop() // Agar gambar rapi memenuhi kotak
            .into(holder.productImage)

        // Listener Tombol
        holder.deleteButton.setOnClickListener {
            onDeleteClick(product)
        }

        holder.editButton.setOnClickListener {
            onEditClick(product)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun updateData(newProductList: List<Product>) {
        this.productList = newProductList
        notifyDataSetChanged()
    }
}