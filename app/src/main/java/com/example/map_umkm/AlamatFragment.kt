package com.example.map_umkm

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
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

class AlamatFragment : Fragment(R.layout.fragment_alamat) {

    private lateinit var rvAddresses: RecyclerView
    private lateinit var btnTambahAlamat: Button
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar

    private lateinit var adapter: AddressAdapter

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAddresses = view.findViewById(R.id.rvAddresses)
        btnTambahAlamat = view.findViewById(R.id.btnTambahAlamat)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        progressBar = view.findViewById(R.id.progressBar)
        toolbar = view.findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        adapter = AddressAdapter(
            mutableListOf(),
            onItemClick = { pilihAlamat(it) },
            onEditClick = { /* fitur lama aman */ },
            onDeleteClick = { konfirmasiHapus(it) },
            onSetPrimaryClick = { setPrimary(it) },
            onUseClick = { pilihAlamat(it) }
        )

        rvAddresses.layoutManager = LinearLayoutManager(requireContext())
        rvAddresses.adapter = adapter

        btnTambahAlamat.setOnClickListener {
            findNavController().navigate(
                R.id.action_alamatFragment_to_pilihLokasiFragment
            )
        }

        loadAlamat()
    }

    private fun loadAlamat() {
        val userId = uid ?: return

        progressBar.visibility = View.VISIBLE

        db.collection("users")
            .document(userId)
            .collection("addresses")
            .orderBy("primary", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                progressBar.visibility = View.GONE

                val list = mutableListOf<Address>()
                snapshot?.documents?.forEach { doc ->
                    doc.toObject(Address::class.java)?.apply {
                        id = doc.id
                        list.add(this)
                    }
                }

                tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(list)
            }
    }

    private fun pilihAlamat(address: Address) {
        setFragmentResult(
            "request_alamat",
            Bundle().apply {
                putParcelable("data_alamat", address)
            }
        )
        findNavController().popBackStack()
    }

    private fun konfirmasiHapus(address: Address) {
        val userId = uid ?: return
        val id = address.id ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Alamat")
            .setMessage("Yakin ingin menghapus alamat ini?")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(id)
                    .delete()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setPrimary(address: Address) {
        val userId = uid ?: return
        val id = address.id ?: return

        val ref = db.collection("users")
            .document(userId)
            .collection("addresses")

        ref.whereEqualTo("primary", true).get()
            .addOnSuccessListener { docs ->
                val batch = db.batch()
                docs.forEach { batch.update(it.reference, "primary", false) }
                batch.update(ref.document(id), "primary", true)
                batch.commit()
            }
    }
}
