package com.example.map_umkm.ui.pesanan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.R
import com.google.firebase.firestore.FirebaseFirestore

class PesananSayaFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pesanan_saya, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        
        val progressBar = view.findViewById<ProgressBar>(R.id.progressPesanan)
        progressBar.progress = 70 

        
        val orderId = "TK-12345"
        db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    val progressValue = when (status) {
                        "Dibuat" -> 30
                        "Diseduh" -> 60
                        "Siap Diambil" -> 100
                        else -> 10
                    }
                    progressBar.progress = progressValue
                }
            }
    }
}
