package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.databinding.FragmentAddEditAddressBinding
import com.example.map_umkm.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.LatLng

class AddEditAddressFragment : Fragment() {

    private var _binding: FragmentAddEditAddressBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    private var addressId: String? = null
    private var isEditMode = false

    // [BARU] Variabel untuk menyimpan koordinat yang dipilih dari peta
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addressId = arguments?.getString("addressId")
        isEditMode = addressId != null

        setupToolbar()
        if (isEditMode) loadExistingAddress()

        setupMapInteraction()

        binding.btnSaveAddress.setOnClickListener {
            saveAddress()
        }
    }

    private fun setupMapInteraction() {
        // [MODIFIKASI] Terima objek Address parsial dari peta
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Address>("selectedLocation")
            ?.observe(viewLifecycleOwner) { partialAddress ->
                binding.etFullAddress.setText(partialAddress.fullAddress)
                // Simpan koordinatnya
                selectedLatitude = partialAddress.latitude
                selectedLongitude = partialAddress.longitude
                // Hapus agar tidak terpanggil lagi
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Address>("selectedLocation")
            }

        binding.btnPilihDiPeta.setOnClickListener {
            findNavController().navigate(R.id.action_addEditAddressFragment_to_pilihLokasiFragment)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.title = if (isEditMode) "Edit Alamat" else "Tambah Alamat Baru"
    }

    private fun loadExistingAddress() {
        val uid = user?.uid ?: return
        db.collection("users").document(uid).collection("addresses").document(addressId!!)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val address = doc.toObject(Address::class.java)
                    binding.etAddressLabel.setText(address?.label ?: "")
                    binding.etRecipientName.setText(address?.recipientName ?: "")
                    binding.etPhoneNumber.setText(address?.phoneNumber ?: "")
                    binding.etFullAddress.setText(address?.fullAddress ?: "")
                    binding.cbSetAsPrimary.isChecked = address?.isPrimary ?: false
                    // [BARU] Muat juga koordinat yang sudah ada
                    selectedLatitude = address?.latitude
                    selectedLongitude = address?.longitude
                }
            }
    }

    private fun saveAddress() {
        val uid = user?.uid ?: return

        val label = binding.etAddressLabel.text.toString().trim()
        val recipient = binding.etRecipientName.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()
        val fullAddress = binding.etFullAddress.text.toString().trim()
        val isPrimary = binding.cbSetAsPrimary.isChecked

        if (label.isEmpty() || recipient.isEmpty() || phone.isEmpty() || fullAddress.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // [MODIFIKASI] Tambahkan latitude dan longitude ke data yang disimpan
        val addressData = mapOf(
            "uid" to uid,
            "label" to label,
            "recipientName" to recipient,
            "phoneNumber" to phone,
            "fullAddress" to fullAddress,
            "isPrimary" to isPrimary,
            "latitude" to selectedLatitude,
            "longitude" to selectedLongitude
        )

        val collection = db.collection("users").document(uid).collection("addresses")

        if (isPrimary) {
            collection.whereEqualTo("isPrimary", true).get().addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isPrimary", false)
                }
                val docRef = if (isEditMode) collection.document(addressId!!) else collection.document()
                batch.set(docRef, addressData)
                batch.commit().addOnSuccessListener {
                    Toast.makeText(requireContext(), "Alamat berhasil disimpan", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        } else {
            val docRef = if (isEditMode) collection.document(addressId!!) else collection.document()
            docRef.set(addressData).addOnSuccessListener {
                Toast.makeText(requireContext(), "Alamat berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
