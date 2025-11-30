package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.AddressAdapter
import com.example.map_umkm.databinding.FragmentAlamatBinding
import com.example.map_umkm.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AlamatFragment : Fragment() {

    private var _binding: FragmentAlamatBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var addressAdapter: AddressAdapter
    private var addressListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlamatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadAddresses()
    }

    override fun onStop() {
        super.onStop()
        addressListener?.remove() // Hentikan listener untuk hemat resource
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnTambahAlamat.setOnClickListener {
            val action = AlamatFragmentDirections.actionAlamatFragmentToAddEditAddressFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        addressAdapter = AddressAdapter(
            emptyList(),
            onEdit = { address ->
                // Kirim ID alamat ke halaman edit
                val action = AlamatFragmentDirections.actionAlamatFragmentToAddEditAddressFragment(address.id)
                findNavController().navigate(action)
            },
            onDelete = { address ->
                // Hapus alamat dari Firestore
                db.collection("addresses").document(address.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Alamat berhasil dihapus", Toast.LENGTH_SHORT).show()
                    }
            },
            onSetPrimary = { address ->
                setPrimaryAddress(address)
            }
        )
        binding.rvAddresses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addressAdapter
        }
    }

    private fun loadAddresses() {
        val uid = auth.currentUser?.uid ?: return
        addressListener?.remove()

        addressListener = db.collection("addresses")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || !isAdded) {
                    return@addSnapshotListener
                }

                val addresses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Address::class.java)?.apply { id = doc.id }
                }?.sortedByDescending { it.isPrimary } ?: emptyList()

                // Logika visibilitas yang benar
                val isEmpty = addresses.isEmpty()
                binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.rvAddresses.visibility = if (isEmpty) View.GONE else View.VISIBLE

                addressAdapter.updateData(addresses)
            }
    }

    private fun setPrimaryAddress(newPrimaryAddress: Address) {
        val uid = auth.currentUser?.uid ?: return
        val batch = db.batch()

        db.collection("addresses").whereEqualTo("uid", uid).get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    // Gunakan logika boolean: true jika ID-nya cocok, false jika tidak
                    batch.update(doc.reference, "isPrimary", (doc.id == newPrimaryAddress.id))
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(context, "${newPrimaryAddress.label} dijadikan alamat utama", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
