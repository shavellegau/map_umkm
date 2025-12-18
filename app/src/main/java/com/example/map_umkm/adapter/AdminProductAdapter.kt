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

        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val priceToShow = product.price_hot ?: product.price_iced ?: 0
        holder.productPrice.text = format.format(priceToShow)

        Glide.with(holder.itemView.context)
            .load(product.image)
            .placeholder(R.drawable.logo_tuku)
            .error(R.drawable.error_image)
            .centerCrop()
            .into(holder.productImage)

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