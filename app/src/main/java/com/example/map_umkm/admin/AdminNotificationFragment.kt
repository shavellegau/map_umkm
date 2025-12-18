package com.example.map_umkm.admin

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.FCMService
import com.example.map_umkm.R
import com.example.map_umkm.adapter.BroadcastAdapter
import com.example.map_umkm.model.BroadcastHistory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminNotificationFragment : Fragment() {

    private lateinit var fcmService: FCMService
    private lateinit var btnBuatBroadcast: Button
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: BroadcastAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_notification, container, false)

        fcmService = FCMService(requireContext())
        btnBuatBroadcast = view.findViewById(R.id.btnBuatBroadcast)
        rvHistory = view.findViewById(R.id.rvBroadcastHistory)

        setupRecyclerView()
        loadHistory()

        btnBuatBroadcast.setOnClickListener {
            showPromoDialog() 
        }

        return view
    }

    private fun setupRecyclerView() {
        
        adapter = BroadcastAdapter(emptyList()) { historyItem ->
            showOptionsDialog(historyItem)
        }
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter
    }

    private fun loadHistory() {
        
        db.collection("broadcast_history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AdminNotif", "Error load history", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    
                    val historyList = snapshots.toObjects(BroadcastHistory::class.java)
                    adapter.updateList(historyList)
                }
            }
    }

    
    
    

    private fun showOptionsDialog(item: BroadcastHistory) {
        val options = arrayOf("Edit", "Hapus")
        AlertDialog.Builder(requireContext())
            .setTitle("Kelola Broadcast")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(item) 
                    1 -> showDeleteConfirmation(item) 
                }
            }
            .show()
    }

    
    private fun showDeleteConfirmation(item: BroadcastHistory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Riwayat?")
            .setMessage("Data '${item.title}' akan dihapus dari riwayat admin.")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("broadcast_history").document(item.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal hapus: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    
    private fun showEditDialog(item: BroadcastHistory) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Riwayat Broadcast")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputTitle = EditText(requireContext())
        inputTitle.setText(item.title) 
        layout.addView(inputTitle)

        val inputBody = EditText(requireContext())
        inputBody.setText(item.body) 
        layout.addView(inputBody)

        builder.setView(layout)

        builder.setPositiveButton("Update") { _, _ ->
            val newTitle = inputTitle.text.toString().trim()
            val newBody = inputBody.text.toString().trim()

            if (newTitle.isNotEmpty()) {
                
                db.collection("broadcast_history").document(item.id)
                    .update(
                        mapOf(
                            "title" to newTitle,
                            "body" to newBody
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(context, "Data diperbarui", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal update: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showPromoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kirim Broadcast Promo")
        builder.setMessage("Pesan akan dikirim ke semua user (Tab Promo).")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputTitle = EditText(requireContext())
        inputTitle.hint = "Judul Promo"
        layout.addView(inputTitle)

        val inputBody = EditText(requireContext())
        inputBody.hint = "Isi Pesan Promo"
        layout.addView(inputBody)

        builder.setView(layout)

        builder.setPositiveButton("Kirim") { _, _ ->
            val title = inputTitle.text.toString().trim()
            val body = inputBody.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(context, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            
            saveToHistory(title, body)

            
            fcmService.sendNotification("promo", title, body)

            Toast.makeText(context, "Broadcast dikirim!", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun saveToHistory(title: String, body: String) {
        val historyMap = hashMapOf(
            "title" to title,
            "body" to body,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("broadcast_history")
            .add(historyMap)
            .addOnFailureListener {
                Toast.makeText(context, "Gagal simpan riwayat: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}