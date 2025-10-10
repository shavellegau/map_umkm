package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Product
import java.text.NumberFormat
import java.util.Locale

class WishlistAdapter(
    private val items: MutableList<Product>,
    private val onRemoveFavorite: (Product) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val imgMenu: ImageView = v.findViewById(R.id.imgMenu)
        val txtName: TextView = v.findViewById(R.id.txtName)
        val txtPrice: TextView = v.findViewById(R.id.txtPrice)
        val btnFav: ImageButton = v.findViewById(R.id.btnFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_wishlist, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.txtName.text = p.name
        holder.txtPrice.text = NumberFormat.getCurrencyInstance(Locale("id","ID")).format(p.price)
        if (p.imageRes != 0) holder.imgMenu.setImageResource(p.imageRes)
        holder.btnFav.setOnClickListener { onRemoveFavorite(p) }
    }

    override fun getItemCount(): Int = items.size

    fun update(itemsNew: List<Product>) {
        items.clear()
        items.addAll(itemsNew)
        notifyDataSetChanged()
    }
}
