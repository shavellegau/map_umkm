package com.example.map_umkm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.AddressAdapter
import com.example.map_umkm.databinding.FragmentAlamatBinding
import com.example.map_umkm.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

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
        setupToolbar()
        setupRecycler()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        loadAddresses()
    }

    override fun onStop() {
        super.onStop()
        addressListener?.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecycler() {
        addressAdapter = AddressAdapter(
            addresses = emptyList(),
            onItemClick = { address ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedAddress", address)
                findNavController().popBackStack()
            },
            onEditClick = { address ->
                if (address.id.isNotEmpty()) {
                    val action = AlamatFragmentDirections.actionAlamatFragmentToAddEditAddressFragment(address.id)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(context, "ID alamat tidak valid untuk diedit.", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { address ->
                showDeleteConfirmation(address)
            },
            onSetPrimaryClick = { address ->
                setPrimaryAddress(address)
            }
        )

        binding.rvAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAddresses.adapter = addressAdapter
    }

    private fun setupButtons() {
        binding.btnTambahAlamat.setOnClickListener {
            val action = AlamatFragmentDirections.actionAlamatFragmentToAddEditAddressFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun setLoading(show: Boolean) {
        if (_binding != null) {
            binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
            if (show) {
                binding.rvAddresses.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
            }
        }
    }

    /**
     * [FIXED] Mengisi properti 'id' dari setiap dokumen ke dalam model Address.
     */
    private fun loadAddresses() {
        setLoading(true)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Sesi tidak valid.", Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }

        addressListener?.remove()

        addressListener = db.collection("users").document(uid).collection("addresses")
            .orderBy("isPrimary", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                setLoading(false)

                if (error != null) {
                    Log.w("AlamatFragment", "Gagal memuat alamat: ", error)
                    binding.tvEmpty.text = "Gagal memuat alamat. Periksa koneksi."
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvAddresses.visibility = View.GONE
                    return@addSnapshotListener
                }

                if (!isAdded || _binding == null) return@addSnapshotListener

                
                val addresses = mutableListOf<Address>()
                snapshot?.documents?.forEach { doc ->
                    val address = doc.toObject(Address::class.java)
                    if (address != null) {
                        
                        address.id = doc.id
                        addresses.add(address)
                    }
                }

                addressAdapter.updateData(addresses)

                if (addresses.isEmpty()) {
                    binding.tvEmpty.text = "Anda belum punya alamat tersimpan."
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvAddresses.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvAddresses.visibility = View.VISIBLE
                }
            }
    }

    private fun showDeleteConfirmation(address: Address) {
        val uid = auth.currentUser?.uid ?: return
        if (address.id.isEmpty()) {
            Toast.makeText(requireContext(), "Error: ID Alamat tidak ditemukan untuk dihapus.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Alamat")
            .setMessage("Anda yakin ingin menghapus alamat '${address.label}'?")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("users").document(uid).collection("addresses").document(address.id)
                    .delete()
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Gagal hapus: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setPrimaryAddress(newPrimaryAddress: Address) {
        val uid = auth.currentUser?.uid ?: return

        if (newPrimaryAddress.id.isEmpty()) {
            Toast.makeText(requireContext(), "Error: ID Alamat tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        val batch = db.batch()

        db.collection("users").document(uid).collection("addresses").get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isPrimary", (doc.id == newPrimaryAddress.id))
                }

                batch.commit().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Alamat utama berhasil diubah", Toast.LENGTH_SHORT).show()
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedAddress", newPrimaryAddress)
                        findNavController().popBackStack()
                    } else {
                        setLoading(false)
                        Toast.makeText(context, "Gagal mengubah alamat utama", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Log.e("AlamatFragment", "Gagal mengambil data untuk set primary", e)
                Toast.makeText(context, "Gagal mengambil data alamat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
