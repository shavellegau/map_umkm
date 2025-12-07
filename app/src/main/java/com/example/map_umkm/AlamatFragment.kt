package com.example.map_umkm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        setupRecyclerView()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        loadAddresses()
    }

    override fun onStop() {
        super.onStop()
        addressListener?.remove()
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
            onItemClick = { selectedAddress ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedAddress", selectedAddress)
                Toast.makeText(context, "Menggunakan alamat '${selectedAddress.label}'", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            },
            onEdit = { address ->
                val action = AlamatFragmentDirections.actionAlamatFragmentToAddEditAddressFragment(address.id)
                findNavController().navigate(action)
            },
            onDelete = { address -> showDeleteConfirmation(address) },
            onSetPrimary = { address -> setPrimaryAddress(address) }
        )
        binding.rvAddresses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addressAdapter
        }
    }

    private fun loadAddresses() {
        setLoading(true)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Sesi tidak valid, silakan login ulang.", Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }

        addressListener?.remove()

        addressListener = db.collection("users").document(uid).collection("addresses")
            .orderBy("isPrimary", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                setLoading(false)
                if (error != null) {
                    Log.w("AlamatFragment", "Gagal memuat alamat", error)
                    Toast.makeText(context, "Gagal memuat alamat.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (!isAdded || _binding == null) return@addSnapshotListener

                val addresses = snapshot?.toObjects(Address::class.java) ?: emptyList()

                if (addresses.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvAddresses.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvAddresses.visibility = View.VISIBLE
                }

                addressAdapter.updateData(addresses)
            }
    }

    private fun setPrimaryAddress(newPrimaryAddress: Address) {
        val uid = auth.currentUser?.uid ?: return
        setLoading(true)
        val batch = db.batch()

        db.collection("users").document(uid).collection("addresses").get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isPrimary", (doc.id == newPrimaryAddress.id))
                }
                batch.commit().addOnCompleteListener { task ->
                    setLoading(false)
                    if(task.isSuccessful){
                        Toast.makeText(context, "'${newPrimaryAddress.label}' dijadikan alamat utama", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal mengubah alamat utama", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Log.e("AlamatFragment", "Gagal set primary", e)
                Toast.makeText(context, "Gagal mengambil data alamat", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation(address: Address) {
        val uid = auth.currentUser?.uid ?: return
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Alamat")
            .setMessage("Anda yakin ingin menghapus alamat '${address.label}'?")
            .setPositiveButton("Hapus") { _, _ ->
                db.collection("users").document(uid).collection("addresses").document(address.id).delete()
                    .addOnFailureListener { e ->
                        Log.e("AlamatFragment", "Gagal hapus", e)
                        Toast.makeText(context, "Gagal menghapus alamat", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isAdded && _binding != null) {
            binding.btnTambahAlamat.isEnabled = !isLoading
            // Jika Anda punya ProgressBar, atur visibilitasnya di sini
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
