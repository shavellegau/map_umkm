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
        setupListeners()

        if (currentAddressId == null) {
            binding.toolbar.title = "Tambah Alamat Baru"
        } else {
            binding.toolbar.title = "Ubah Alamat"
            // [FIXED] Panggil fungsi load dengan UID
            auth.currentUser?.uid?.let { uid ->
                loadAddressData(uid, currentAddressId!!)
            }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnSaveAddress.setOnClickListener {
            handleSave()
        }
    }

    /**
     * [FIXED] Memuat data dari subcollection users/{uid}/addresses/{addressId}
     */
    private fun loadAddressData(uid: String, addressId: String) {
        setLoading(true)
        db.collection("users").document(uid).collection("addresses").document(addressId).get()
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

    /**
     * [FIXED] Menyimpan data ke subcollection users/{uid}/addresses/{addressId}
     */
    private fun saveAddressToFirestore(
        uid: String, label: String, recipientName: String, phone: String, fullAddress: String, isPrimary: Boolean
    ) {
        // [PERBAIKAN UTAMA] Path sekarang menunjuk ke subcollection di bawah user
        val collectionRef = db.collection("users").document(uid).collection("addresses")

        val docRef = if (currentAddressId == null) {
            collectionRef.document() // Buat dokumen baru di dalam subcollection
        } else {
            collectionRef.document(currentAddressId!!) // Tunjuk ke dokumen yang ada
        }

        // `uid` di dalam objek Address sebenarnya redundan jika sudah di subcollection, tapi tidak apa-apa untuk disimpan
        val address = Address(docRef.id, uid, label, recipientName, phone, fullAddress, null, isPrimary)

        if (isPrimary) {
            unsetOtherPrimaryAndSave(address, docRef)
        } else {
            docRef.set(address).addOnCompleteListener { task ->
                onSaveComplete(task.isSuccessful)
            }
        }
    }

    /**
     * [FIXED] Mengubah status primary pada alamat lain di subcollection yang sama
     */
    private fun unsetOtherPrimaryAndSave(addressToSave: Address, docRef: DocumentReference) {
        val uid = auth.currentUser?.uid ?: run { onSaveComplete(false); return }
        val batch = db.batch()

        // [PERBAIKAN UTAMA] Query ke subcollection yang benar
        db.collection("users").document(uid).collection("addresses").whereEqualTo("isPrimary", true).get()
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
        if (isAdded && _binding != null) {
            binding.btnSaveAddress.isEnabled = !isLoading
            binding.btnSaveAddress.text = if (isLoading) "Menyimpan..." else "Simpan Alamat"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
