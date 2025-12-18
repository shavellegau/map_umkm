package com.example.map_umkm.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.AdminCabangAdapter
import com.example.map_umkm.databinding.FragmentAdminCabangBinding
import com.example.map_umkm.model.Cabang
import com.google.firebase.firestore.FirebaseFirestore

class AdminCabangFragment : Fragment() {

    private var _binding: FragmentAdminCabangBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminCabangAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCabangBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadDataCabang()

        binding.fabAddCabang.setOnClickListener {
            
            Toast.makeText(context, "Tambah Cabang diklik", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        
        adapter = AdminCabangAdapter(
            listCabang = listOf(),
            onEditClick = { cabang ->
                Toast.makeText(context, "Edit: ${cabang.nama}", Toast.LENGTH_SHORT).show()
                
            },
            onDeleteClick = { cabang ->
                deleteCabang(cabang)
            }
        )

        binding.rvAdminCabang.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminCabangFragment.adapter
        }
    }

    private fun loadDataCabang() {
        db.collection("cabang")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val list = value?.toObjects(Cabang::class.java) ?: listOf()
                adapter.updateData(list)
            }
    }

    private fun deleteCabang(cabang: Cabang) {
        if (cabang.id.isNotEmpty()) {
            db.collection("cabang").document(cabang.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Cabang dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}