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
        // --- PERBAIKAN: Menggunakan ID baru dari layout yang sudah di-redesign ---
        val productImage: ImageView = itemView.findViewById(R.id.ivProductImage) // ID baru
        val productName: TextView = itemView.findViewById(R.id.tvProductName)     // ID baru
        val productPrice: TextView = itemView.findViewById(R.id.tvProductPrice)    // ID baru
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)        // ID baru
        val editButton: ImageButton = itemView.findViewById(R.id.btnEdit)          // ID baru
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductViewHolder {
        // Menggunakan layout item_admin_product.xml yang sudah di-redesign
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

        // Load gambar menggunakan Glide
        val context = holder.itemView.context
        val imageResId = context.resources.getIdentifier(product.image, "drawable", context.packageName)

        Glide.with(context)
            .load(if (imageResId != 0) imageResId else R.drawable.logo_tuku) // Fallback ke logo_tuku
            .placeholder(R.drawable.logo_tuku)
            .error(R.drawable.logo_tuku)
            .into(holder.productImage)

        // --- PERBAIKAN: Menambahkan listener untuk tombol edit yang baru ---
        holder.deleteButton.setOnClickListener {
            onDeleteClick(product)
        }

        holder.editButton.setOnClickListener {
            onEditClick(product)
        }

        // --- PERBAIKAN: Sebaiknya klik pada item juga memicu edit, bukan di seluruh itemView ---
        // Jika ingin klik di mana saja pada kartu untuk edit, baris ini bisa diaktifkan kembali.
        // Namun, karena sudah ada tombol edit, ini bisa jadi redundan.
        // holder.itemView.setOnClickListener {
        //     onEditClick(product)
        // }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun updateData(newProductList: List<Product>) {
        this.productList = newProductList
        notifyDataSetChanged() // Untuk kesederhanaan. Bisa dioptimalkan dengan DiffUtil jika perlu.
    }
}
