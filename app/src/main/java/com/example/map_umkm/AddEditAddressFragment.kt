package com.example.map_umkm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.map_umkm.databinding.FragmentAddEditAddressBinding
import com.example.map_umkm.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class AddEditAddressFragment : Fragment() {

    private var _binding: FragmentAddEditAddressBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditAddressFragmentArgs by navArgs()
    private var currentAddressId: String? = null

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentAddressId = args.addressId
        setupListeners() // Pemasangan listener lebih awal

        if (currentAddressId == null) {
            binding.toolbar.title = "Tambah Alamat Baru"
        } else {
            binding.toolbar.title = "Ubah Alamat"
            loadAddressData(currentAddressId!!)
        }
    }

    private fun setupListeners() {
        // [FIXED] Listener ini akan berfungsi setelah masalah login & dependensi diselesaikan
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnSaveAddress.setOnClickListener {
            handleSave()
        }
    }

    private fun loadAddressData(id: String) {
        setLoading(true)
        db.collection("addresses").document(id).get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
                if (document != null && document.exists()) {
                    document.toObject(Address::class.java)?.let { populateFields(it) }
                }
                setLoading(false)
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                setLoading(false)
                Toast.makeText(context, "Gagal memuat data alamat.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFields(address: Address) {
        binding.etAddressLabel.setText(address.label)
        binding.etRecipientName.setText(address.recipientName)
        binding.etPhoneNumber.setText(address.phoneNumber)
        binding.etFullAddress.setText(address.fullAddress)
        binding.cbSetAsPrimary.isChecked = address.isPrimary
    }

    private fun handleSave() {
        // [FIXED] Ambil UID dari instance auth. Setelah login via Firebase, ini PASTI ada.
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            Log.e("AddEditAddress", "Gagal menyimpan: UID pengguna null. Sesi login Firebase tidak ada.")
            Toast.makeText(context, "Gagal mendapatkan sesi. Silakan login ulang.", Toast.LENGTH_LONG).show()
            setLoading(false)
            return
        }

        val label = binding.etAddressLabel.text.toString().trim()
        val recipientName = binding.etRecipientName.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()
        val fullAddress = binding.etFullAddress.text.toString().trim()

        if (label.isEmpty() || recipientName.isEmpty() || phone.isEmpty() || fullAddress.isEmpty()) {
            Toast.makeText(context, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        saveAddressToFirestore(uid, label, recipientName, phone, fullAddress, binding.cbSetAsPrimary.isChecked)
    }

    private fun saveAddressToFirestore(
        uid: String, label: String, recipientName: String, phone: String, fullAddress: String, isPrimary: Boolean
    ) {
        val docRef = if (currentAddressId == null) {
            db.collection("addresses").document()
        } else {
            db.collection("addresses").document(currentAddressId!!)
        }

        val address = Address(docRef.id, uid, label, recipientName, phone, fullAddress, null, isPrimary)

        if (isPrimary) {
            unsetOtherPrimaryAndSave(address, docRef)
        } else {
            docRef.set(address).addOnCompleteListener { task ->
                onSaveComplete(task.isSuccessful)
            }
        }
    }

    private fun unsetOtherPrimaryAndSave(addressToSave: Address, docRef: DocumentReference) {
        val uid = auth.currentUser?.uid ?: run { onSaveComplete(false); return }
        val batch = db.batch()

        db.collection("addresses").whereEqualTo("uid", uid).whereEqualTo("isPrimary", true).get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    if (doc.id != addressToSave.id) {
                        batch.update(doc.reference, "isPrimary", false)
                    }
                }
                batch.set(docRef, addressToSave)
                batch.commit().addOnCompleteListener { task ->
                    onSaveComplete(task.isSuccessful)
                }
            }
            .addOnFailureListener {
                Log.e("AddEditAddress", "Gagal mencari alamat utama lama.", it)
                onSaveComplete(false)
            }
    }

    private fun onSaveComplete(isSuccess: Boolean) {
        if (!isAdded) return
        setLoading(false)
        if (isSuccess) {
            Toast.makeText(context, "Alamat berhasil disimpan!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        } else {
            Toast.makeText(context, "Gagal menyimpan alamat. Terjadi kesalahan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (!isAdded) return
        binding.btnSaveAddress.isEnabled = !isLoading
        binding.btnSaveAddress.text = if (isLoading) "Menyimpan..." else "Simpan Alamat"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
