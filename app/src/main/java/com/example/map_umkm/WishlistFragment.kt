package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.WishlistAdapter
import com.example.map_umkm.databinding.FragmentWishlistBinding
import com.example.map_umkm.viewmodel.FavoriteViewModel

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WishlistAdapter
    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteViewModel.loadFavorites()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        observeFavorites()
    }


    private fun setupRecyclerView() {
        adapter = WishlistAdapter(
            favoriteList = mutableListOf(),
            onProductClick = { product ->
                try {
                    val action = WishlistFragmentDirections
                        .actionWishlistFragmentToProductDetailFragment(product)


                    val bundle = action.arguments
                    bundle.putString("source", "wishlist")

                    findNavController().navigate(R.id.productDetailFragment, bundle)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Gagal membuka detail produk",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onFavoriteToggle = { product, isFavorite ->
                try {
                    if (isFavorite) {
                        favoriteViewModel.addFavorite(product)
                        Toast.makeText(
                            requireContext(),
                            "${product.name} ditambahkan ke favorit",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        favoriteViewModel.removeFavorite(product)
                        Toast.makeText(
                            requireContext(),
                            "${product.name} dihapus dari favorit",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Terjadi kesalahan pada favorit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        binding.recyclerWishlist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWishlist.adapter = adapter
    }

    private fun observeFavorites() {
        favoriteViewModel.favoriteProducts.observe(viewLifecycleOwner) { favorites ->
            adapter.updateData(favorites ?: emptyList())
            binding.emptyView.visibility =
                if (favorites.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
