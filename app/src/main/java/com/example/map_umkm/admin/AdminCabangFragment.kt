package com.example.map_umkm.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.R
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
            showDialogForm(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminCabangAdapter(
            listCabang = listOf(),
            onEditClick = { cabang ->
                showDialogForm(cabang)
            },
            onDeleteClick = { cabang ->
                showDeleteConfirmation(cabang)
            }
        )

        binding.rvAdminCabang.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminCabangFragment.adapter
        }
    }

    private fun showDialogForm(cabang: Cabang?) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_form_cabang, null)

        builder.setView(dialogView)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitleDialog)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaCabang)
        val etAlamat = dialogView.findViewById<EditText>(R.id.etAlamatCabang)
        val etBuka = dialogView.findViewById<EditText>(R.id.etJamBuka)
        val etTutup = dialogView.findViewById<EditText>(R.id.etJamTutup)
        val etFasilitas = dialogView.findViewById<EditText>(R.id.etFasilitas)
        val etLat = dialogView.findViewById<EditText>(R.id.etLatitude)
        val etLong = dialogView.findViewById<EditText>(R.id.etLongitude)

        if (cabang != null) {
            tvTitle.text = "Edit Cabang"
            etNama.setText(cabang.nama)
            etAlamat.setText(cabang.alamat)
            etBuka.setText(cabang.jamBuka)
            etTutup.setText(cabang.jamTutup)
            etFasilitas.setText(cabang.fasilitas)
            etLat.setText(cabang.latitude.toString())
            etLong.setText(cabang.longitude.toString())
        } else {
            tvTitle.text = "Tambah Cabang Baru"
        }

        builder.setPositiveButton("Simpan") { _, _ ->
            val nama = etNama.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()
            val jamBuka = etBuka.text.toString().trim()
            val jamTutup = etTutup.text.toString().trim()
            val fasilitas = etFasilitas.text.toString().trim()
            val latitude = etLat.text.toString().toDoubleOrNull() ?: 0.0
            val longitude = etLong.text.toString().toDoubleOrNull() ?: 0.0

            if (nama.isEmpty() || alamat.isEmpty()) {
                Toast.makeText(context, "Nama dan Alamat wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val dataCabang = hashMapOf(
                "nama" to nama,
                "alamat" to alamat,
                "jamBuka" to jamBuka,
                "jamTutup" to jamTutup,
                "fasilitas" to fasilitas,
                "latitude" to latitude,
                "longitude" to longitude
            )

            if (cabang == null) {
                db.collection("branches").add(dataCabang)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Berhasil menambah cabang!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Gagal tambah: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                db.collection("branches").document(cabang.id)
                    .update(dataCabang as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Data berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Gagal update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showDeleteConfirmation(cabang: Cabang) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Cabang")
            .setMessage("Yakin ingin menghapus cabang ${cabang.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteCabang(cabang)
            }
            .setNegativeButton("Batal", null)
            .show()
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}