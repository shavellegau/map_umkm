package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.WishlistAdapter
import com.example.map_umkm.helper.FirestoreHelper
import com.example.map_umkm.model.Product

class WishlistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WishlistAdapter
    private val favoriteItems = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wishlist, container, false)

        recyclerView = view.findViewById(R.id.recyclerWishlist)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Use WishlistAdapter and pass the correct lambda for item removal
        adapter = WishlistAdapter(favoriteItems, onRemoveFavorite = { product ->
            FirestoreHelper.removeFromWishlist(product) { success ->
                if (success) {
                    val position = favoriteItems.indexOf(product)
                    if (position != -1) {
                        favoriteItems.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        Toast.makeText(requireContext(), "${product.name} dihapus dari favorit", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus item", Toast.LENGTH_SHORT).show()
                }
            }
        })

        recyclerView.adapter = adapter
        loadFavorites()

        return view
    }

    private fun loadFavorites() {
        FirestoreHelper.getWishlist { data ->
            if (data.isEmpty()) {
                Toast.makeText(requireContext(), "Belum ada item favorit", Toast.LENGTH_SHORT).show()
            } else {
                val productList = data.mapNotNull { map ->
                    try {
                        Product(
                            id = (map["id"] as? Number)?.toInt() ?: 0,
                            name = map["name"] as? String ?: "",
                            price_hot = (map["price_hot"] as? Number)?.toInt() ?: 0, // Correctly access price_hot
                            price_iced = (map["price_iced"] as? Number)?.toInt(),
                            image = map["image"] as? String, // Correctly access image URL
                            category = map["category"] as? String ?: "",
                            isFavorite = true,
                            description = map["description"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                favoriteItems.clear()
                favoriteItems.addAll(productList)
                adapter.notifyDataSetChanged()
            }
        }
    }
}