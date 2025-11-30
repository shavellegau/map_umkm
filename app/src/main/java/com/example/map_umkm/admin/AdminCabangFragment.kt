package com.example.map_umkm.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.CabangAdapter
import com.example.map_umkm.model.Cabang
import com.example.map_umkm.R
import com.example.map_umkm.databinding.FragmentAdminCabangBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminCabangFragment : Fragment() {

    // 🔥 PERBAIKAN: Menggunakan FragmentAdminCabangBinding
    private var _binding: FragmentAdminCabangBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: CabangAdapter
    private var cabangListener: ListenerRegistration? = null

    // --- Siklus Hidup Fragment ---

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // 🔥 PERBAIKAN: Menggunakan FragmentAdminCabangBinding.inflate
        _binding = FragmentAdminCabangBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        startListeningForBranches()

        // Akses View Binding untuk tombol tambah
        binding.fabAddBranch.setOnClickListener {
            showAddEditDialog(null) // Tambah baru
        }
    }

    // --- Pengaturan RecyclerView dan Data ---

// Di dalam AdminCabangFragment.kt -> private fun setupRecyclerView() { ... }

    private fun setupRecyclerView() {
        // 🔥 PERBAIKAN: Semua argumen dimasukkan ke dalam kurung () 🔥
        // Argumen 1: List data awal
        adapter = CabangAdapter(
            daftarCabang = emptyList(), // Tambahkan List data awal (diberi nama eksplisit)
            ketikaEditDiklik = { cabang -> showAddEditDialog(cabang) },
            ketikaHapusDiklik = { cabang -> confirmDelete(cabang) }
        )

        // Pastikan rvAdminBranches adalah ID RecyclerView di layout FragmentAdminBranchBinding
        binding.rvAdminBranches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminBranches.adapter = adapter
    }

    private fun startListeningForBranches() {
        cabangListener?.remove()
        cabangListener = db.collection("branches")
            .orderBy("nama")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AdminCabang", "Gagal memuat data cabang: ${e.message}", e)
                    Toast.makeText(context, "Gagal memuat data cabang: ${e.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                val daftarCabangBaru = mutableListOf<Cabang>()
                snapshots?.documents?.forEach { document ->
                    val cabang = document.toObject(Cabang::class.java)
                    if (cabang != null) {
                        cabang.id = document.id
                        daftarCabangBaru.add(cabang)
                    }
                }

                adapter.updateData(daftarCabangBaru)
            }
    }

    // --- Fungsi CRUD: Tambah/Edit Cabang ---

    private fun showAddEditDialog(cabang: Cabang?) {
        // Inflate layout dialog, diasumsikan R.layout.dialog_add_edit_branch ada
        val tampilanDialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_branch, null)

        // Akses elemen UI menggunakan findViewById pada tampilanDialog
        val etNama: EditText = tampilanDialog.findViewById(R.id.et_branch_nama)
        val etAlamat: EditText = tampilanDialog.findViewById(R.id.et_branch_alamat)
        val etJamBuka: EditText = tampilanDialog.findViewById(R.id.et_branch_jam_buka)
        val etStatus: EditText = tampilanDialog.findViewById(R.id.et_branch_status)
        val etDetail: EditText = tampilanDialog.findViewById(R.id.et_branch_detail)
        val tombolSimpan: Button = tampilanDialog.findViewById(R.id.btn_save_branch)
        val tvJudul: TextView = tampilanDialog.findViewById(R.id.tv_dialog_title)

        val apakahMengedit = cabang != null
        tvJudul.text = if (apakahMengedit) "Edit Cabang: ${cabang!!.nama}" else "Tambah Cabang Baru"

        if (apakahMengedit) {
            etNama.setText(cabang!!.nama)
            etAlamat.setText(cabang.alamat)
            etJamBuka.setText(cabang.jamBuka)
            etStatus.setText(cabang.statusBuka)
            etDetail.setText(cabang.detail)
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(tampilanDialog).create()

        tombolSimpan.setOnClickListener {
            val cabangBaru = Cabang(
                id = cabang?.id ?: "",
                nama = etNama.text.toString().trim(),
                alamat = etAlamat.text.toString().trim(),
                jamBuka = etJamBuka.text.toString().trim(),
                statusBuka = etStatus.text.toString().trim(),
                detail = etDetail.text.toString().trim()
            )

            if (cabangBaru.nama.isEmpty() || cabangBaru.alamat.isEmpty()) {
                Toast.makeText(context, "Nama dan Alamat wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val docRef = if (apakahMengedit) {
                db.collection("branches").document(cabang!!.id)
            } else {
                db.collection("branches").document()
            }

            if (!apakahMengedit) {
                cabangBaru.id = docRef.id
            }

            docRef.set(cabangBaru)
                .addOnSuccessListener {
                    Toast.makeText(context, "Cabang berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        dialog.show()
    }

    // --- Fungsi CRUD: Hapus Cabang ---

    private fun confirmDelete(cabang: Cabang) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Cabang")
            .setMessage("Apakah Anda yakin ingin menghapus cabang ${cabang.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("branches").document(cabang.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Cabang ${cabang.nama} berhasil dihapus.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Gagal menghapus: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // --- Cleanup ---

    override fun onDestroyView() {
        super.onDestroyView()
        cabangListener?.remove()
        _binding = null
    }
}