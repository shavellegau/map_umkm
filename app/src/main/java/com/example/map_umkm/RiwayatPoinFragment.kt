package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.PoinHistoryAdapter
import com.example.map_umkm.model.PoinHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RiwayatPoinFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_riwayat_poin, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPoin)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val poinList = listOf(
            PoinHistory("Tambah Poin", "+500", "01 Oktober 2025"),
            PoinHistory("Tukar Voucher", "-2000", "05 Oktober 2025"),
            PoinHistory("Bonus Member Gold", "+1000", "10 Oktober 2025")
        )

        recyclerView.adapter = PoinHistoryAdapter(poinList)
        return view
    }
}

object FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    // Menyimpan produk favorit ke Firestore
    fun addToWishlist(productId: String, productName: String, productPrice: Double, imageUrl: String, onComplete: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onComplete(false)
            return
        }

        val wishlistData = hashMapOf(
            "productId" to productId,
            "productName" to productName,
            "productPrice" to productPrice,
            "imageUrl" to imageUrl
        )

        db.collection("users")
            .document(userId)
            .collection("wishlist")
            .document(productId)
            .set(wishlistData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Menghapus produk dari wishlist
    fun removeFromWishlist(productId: String, onComplete: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onComplete(false)
            return
        }

        db.collection("users")
            .document(userId)
            .collection("wishlist")
            .document(productId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Mengambil semua wishlist user
    fun getWishlist(onResult: (List<Map<String, Any>>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onResult(emptyList())
            return
        }

        db.collection("users")
            .document(userId)
            .collection("wishlist")
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { it.data }
                onResult(items)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}