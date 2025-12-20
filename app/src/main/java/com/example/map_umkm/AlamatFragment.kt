package com.example.map_umkm

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AddressAdapter
import com.example.map_umkm.model.Address
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AlamatFragment : Fragment() {

    private lateinit var rvAddress: RecyclerView
    private lateinit var btnAddAddress: Button
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var addressAdapter: AddressAdapter
    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alamat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Binding View
        rvAddress = view.findViewById(R.id.rvAddresses)
        btnAddAddress = view.findViewById(R.id.btnTambahAlamat)
        toolbar = view.findViewById(R.id.toolbar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        progressBar = view.findViewById(R.id.progressBar)

        // 2. Setup Toolbar Back Button
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 3. Setup RecyclerView
        setupRecyclerView()

        // 4. Load Data
        loadAddresses()

        // 5. Setup Tombol Tambah
        btnAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_alamatFragment_to_addEditAddressFragment)
        }
    }

    private fun setupRecyclerView() {
        // PERBAIKAN: Menambahkan onUseClick yang sebelumnya error
        addressAdapter = AddressAdapter(
            addresses = mutableListOf(),
            onItemClick = { address -> pilihAlamatDanKembali(address) }, // Klik item = pilih
            onEditClick = { address -> navigateToEdit(address) },
            onDeleteClick = { address -> showDeleteConfirmation(address) },
            onSetPrimaryClick = { address -> setPrimaryAddress(address) },
            onUseClick = { address ->
                // Logic tombol "Gunakan"
                pilihAlamatDanKembali(address)
            }
        )

        rvAddress.layoutManager = LinearLayoutManager(context)
        rvAddress.adapter = addressAdapter
    }

    // Fungsi untuk mengirim alamat terpilih ke fragment sebelumnya (misal Home/Checkout)
    private fun pilihAlamatDanKembali(address: Address) {
        setFragmentResult("request_alamat", bundleOf("data_alamat" to address))
        findNavController().popBackStack()
    }

    private fun navigateToEdit(address: Address) {
        val bundle = Bundle().apply {
            putString("addressId", address.id)
        }
        findNavController().navigate(R.id.action_alamatFragment_to_addEditAddressFragment, bundle)
    }

    private fun showDeleteConfirmation(address: Address) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Alamat")
            .setMessage("Apakah Anda yakin ingin menghapus alamat ${address.label}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteAddress(address)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteAddress(address: Address) {
        if (uid == null) return
        db.collection("users").document(uid).collection("addresses").document(address.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Alamat berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setPrimaryAddress(selectedAddress: Address) {
        if (uid == null) return

        progressBar.isVisible = true
        val batch = db.batch()
        val colRef = db.collection("users").document(uid).collection("addresses")

        colRef.get().addOnSuccessListener { docs ->
            for (doc in docs) {
                if (doc.id != selectedAddress.id) {
                    batch.update(doc.reference, "isPrimary", false)
                }
            }
            val selRef = colRef.document(selectedAddress.id)
            batch.update(selRef, "isPrimary", true)

            batch.commit().addOnSuccessListener {
                progressBar.isVisible = false
                Toast.makeText(context, "Alamat utama diperbarui", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progressBar.isVisible = false
            }
        }
    }

    private fun loadAddresses() {
        if (uid == null) return

        progressBar.isVisible = true
        tvEmpty.isVisible = false

        db.collection("users").document(uid).collection("addresses")
            .orderBy("isPrimary", Query.Direction.DESCENDING)
            .addSnapshotListener { snaps, e ->
                progressBar.isVisible = false

                if (e != null) {
                    Toast.makeText(context, "Gagal memuat: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snaps != null) {
                    val list = snaps.toObjects(Address::class.java)
                    for (i in list.indices) {
                        list[i] = list[i].copy(id = snaps.documents[i].id)
                    }

                    addressAdapter.updateData(list)

                    tvEmpty.isVisible = list.isEmpty()
                    rvAddress.isVisible = list.isNotEmpty()
                }
            }
    }
}