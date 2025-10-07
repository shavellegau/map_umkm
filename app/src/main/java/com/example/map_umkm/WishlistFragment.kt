
package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.ProductAdapter
import com.example.map_umkm.model.Product
import com.example.map_umkm.helper.FirestoreHelper



class WishlistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private val favoriteItems = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wishlist, container, false)

        recyclerView = view.findViewById(R.id.recyclerWishlist)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProductAdapter(favoriteItems, { product, isFavorite ->
            // onItemClick listener
        }, { product, isFav ->
            if (!isFav) {
                val position = favoriteItems.indexOf(product)
                if (position != -1) {
                    favoriteItems.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
            }
        })

        recyclerView.adapter = adapter
        loadFavorites()

        return view
    }

// In C:/Users/ferry/AndroidStudioProjects/map_umkm/app/src/main/java/com/example/map_umkm/WishlistFragment.kt

    private fun loadFavorites() {
        FirestoreHelper.getWishlist { data ->
            if (data.isEmpty()) {
                Toast.makeText(requireContext(), "Belum ada item favorit", Toast.LENGTH_SHORT).show()
            } else {
                // 1. Map the generic Map objects to your Product data class
                val productList = data.map { map ->
                    Product(
                        id = (map["id"] as? Number)?.toInt() ?: 0,
                        name = map["name"] as? String ?: "",
                        price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                        imageRes = R.drawable.logo_tuku, // atau drawable default kamu
                        category = map["category"] as? String ?: "",
                        isFavorite = true
                    )
                }

                // 2. Clear the old list and add the new, correctly typed list
                favoriteItems.clear()
                favoriteItems.addAll(productList)
                adapter.notifyDataSetChanged()
            }
        }
    }


}
