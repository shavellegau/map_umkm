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

class ProductAdapter(
    // UBAH 'private' menjadi 'internal' agar bisa diakses dari CartFragment
    internal var products: MutableList<Product>,
    private val onProductClick: (Product) -> Unit,
    private val onFavoriteToggle: (Product, Boolean) -> Unit // ✅ Tambahan listener favorit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnAddToCart: ImageButton = itemView.findViewById(R.id.btnAddToCart)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite) // ✅ Tambahan tombol favorite
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Pastikan Anda sudah membuat R.layout.item_menu
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvProductName.text = product.name

        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(product.price_hot) // Menggunakan price_hot sebagai harga default

        holder.tvProductPrice.text = formattedPrice

        // Muat gambar dari URL menggunakan Glide
        if (!product.image.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.image)
                .placeholder(R.drawable.logo_tuku) // Ganti placeholder jika ada
                .error(R.drawable.logo_tuku)       // Ganti gambar error jika ada
                .into(holder.ivProductImage)
        } else {
            // Muat gambar default jika URL null atau kosong
            holder.ivProductImage.setImageResource(R.drawable.logo_tuku)
        }


        // ✅ Atur icon favorite sesuai status
        holder.btnFavorite.setImageResource(
            if (product.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )

        // ✅ Listener untuk tombol favorite
        holder.btnFavorite.setOnClickListener {
            // Kita tidak perlu mengubah state di sini lagi karena sudah ditangani di CartFragment
            onFavoriteToggle(product, !product.isFavorite)
        }

        // Listener untuk klik item produk
        holder.itemView.setOnClickListener {
            onProductClick(product)
        }

        // Listener untuk tombol cart (tetap seperti semula)
        holder.btnAddToCart.setOnClickListener {
            onProductClick(product) // Arahkan ke detail juga
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}
