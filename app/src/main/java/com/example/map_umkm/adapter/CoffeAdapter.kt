package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Coffee

class CoffeeAdapter(
    private val items: MutableList<Coffee>,
    private val onAddClick: ((Coffee) -> Unit)? = null
) : RecyclerView.Adapter<CoffeeAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.ivCoffeeImage)
        val name: TextView = view.findViewById(R.id.tvCoffeeName)
        val price: TextView = view.findViewById(R.id.tvCoffeePrice)
        val btnAdd: ImageButton? = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val coffee = items[position]
        holder.img.setImageResource(coffee.imageResId)
        holder.name.text = coffee.name
        holder.price.text = coffee.price
        holder.btnAdd?.setOnClickListener { onAddClick?.invoke(coffee) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Coffee>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
