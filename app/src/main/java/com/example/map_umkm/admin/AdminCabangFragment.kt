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
            Toast.makeText(context, "Fitur Tambah segera hadir", Toast.LENGTH_SHORT).show()
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
        db.collection("branches")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(context, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val list = mutableListOf<Cabang>()

                for (doc in value!!) {
                    val cabang = Cabang(
                        id = doc.id,
                        nama = doc.getString("nama") ?: "",
                        alamat = doc.getString("alamat") ?: "",
                        jamBuka = doc.getString("jamBuka") ?: "",
                        jamTutup = doc.getString("jamTutup") ?: "",
                        fasilitas = doc.getString("fasilitas") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0
                    )
                    list.add(cabang)
                }

                adapter.updateData(list)
            }
    }

    private fun deleteCabang(cabang: Cabang) {
        if (cabang.id.isNotEmpty()) {
            db.collection("branches").document(cabang.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Cabang berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Error: ID Cabang tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}