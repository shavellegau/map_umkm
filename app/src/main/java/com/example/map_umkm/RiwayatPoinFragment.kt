
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
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class RiwayatPoinFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        val view = inflater.inflate(R.layout.fragment_riwayat_poin, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPoin)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        
        loadHistory(recyclerView)

        return view
    }

    private fun loadHistory(recyclerView: RecyclerView) {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        
        db.collection("point_transactions")
            .whereEqualTo("userId", userId) 
            .orderBy("timestamp", Query.Direction.DESCENDING) 
            .get()
            .addOnSuccessListener { result ->
                val historyList = mutableListOf<PoinHistory>()
                
                val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))

                for (document in result.documents) {
                    val amount = document.getLong("amount") ?: 0L
                    val description = document.getString("description") ?: "Transaksi Poin"
                    val timestamp = document.getTimestamp("timestamp")

                    val dateString = if (timestamp != null) {
                        dateFormat.format(timestamp.toDate())
                    } else {
                        "Tanggal Tidak Diketahui"
                    }

                    
                    val amountString = if (amount > 0) "+${amount}" else "${amount}"

                    historyList.add(
                        PoinHistory(
                            title = description,
                            amount = amountString,
                            date = dateString
                        )
                    )
                }

                
                recyclerView.adapter = PoinHistoryAdapter(historyList)
            }
            .addOnFailureListener {
                
            }
    }
}