package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.model.Address
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddEditAddressFragment : Fragment() {

    private lateinit var etLabel: EditText
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etFullAddress: EditText
    private lateinit var etDetails: EditText
    private lateinit var etNotes: EditText

    private lateinit var btnPilihPeta: Button
    private lateinit var cbPrimary: CheckBox
    private lateinit var btnSave: Button
    private lateinit var toolbar: MaterialToolbar

    private var tempLat: Double = 0.0
    private var tempLng: Double = 0.0
    private var addressId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binding View
        etLabel = view.findViewById(R.id.etAddressLabel)
        etName = view.findViewById(R.id.etRecipientName)
        etPhone = view.findViewById(R.id.etPhoneNumber)
        etFullAddress = view.findViewById(R.id.etFullAddress)
        etDetails = view.findViewById(R.id.etAddressDetails)
        etNotes = view.findViewById(R.id.etAddressNotes)

        btnPilihPeta = view.findViewById(R.id.btn_pilih_di_peta)
        cbPrimary = view.findViewById(R.id.cbSetAsPrimary)
        btnSave = view.findViewById(R.id.btnSaveAddress)
        toolbar = view.findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // ---------------------------------------------------------
        // LOGIKA PENERIMAAN DATA
        // ---------------------------------------------------------

        // 1. Cek Arguments (Jika dari Peta Langsung atau Edit Mode)
        arguments?.let { args ->
            // A. Cek jika Edit Mode (membawa addressId)
            addressId = args.getString("addressId")
            if (addressId != null) {
                toolbar.title = "Edit Alamat"
                loadAddressData(addressId!!)
            }

            // B. Cek jika Data Peta Direct (membawa hasil_alamat) - MODIFIKASI
            val directAlamat = args.getString("hasil_alamat")
            if (directAlamat != null) {
                etFullAddress.setText(directAlamat)
                tempLat = args.getDouble("hasil_lat")
                tempLng = args.getDouble("hasil_lng")
            }
        }

        // 2. Listener Fragment Result (Jika user klik tombol "Pilih di Peta" dalam form ini)
        parentFragmentManager.setFragmentResultListener("requestKey_lokasi", viewLifecycleOwner) { _, bundle ->
            val alamat = bundle.getString("hasil_alamat")
            val lat = bundle.getDouble("hasil_lat")
            val lng = bundle.getDouble("hasil_lng")

            if (alamat != null) {
                etFullAddress.setText(alamat)
                tempLat = lat
                tempLng = lng
            }
        }

        // ---------------------------------------------------------

        btnPilihPeta.setOnClickListener {
            // Arahkan ke Fragment Peta
            findNavController().navigate(R.id.action_addEditAddressFragment_to_pilihLokasiFragment)
        }

        btnSave.setOnClickListener {
            saveAddress()
        }
    }

    private fun loadAddressData(id: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .collection("addresses").document(id).get()
            .addOnSuccessListener { doc ->
                val addr = doc.toObject(Address::class.java)
                if (addr != null) {
                    etLabel.setText(addr.label)
                    etName.setText(addr.recipientName)
                    etPhone.setText(addr.phoneNumber)
                    etFullAddress.setText(addr.fullAddress)
                    etDetails.setText(addr.details)
                    etNotes.setText(addr.notes)
                    cbPrimary.isChecked = addr.isPrimary
                    tempLat = addr.latitude
                    tempLng = addr.longitude
                }
            }
    }

    private fun saveAddress() {
        val label = etLabel.text.toString().trim()
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val fullAddr = etFullAddress.text.toString().trim()
        val details = etDetails.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        if (label.isEmpty() || fullAddr.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(context, "Mohon lengkapi data wajib", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val ref = if (addressId == null)
            db.collection("users").document(uid).collection("addresses").document()
        else
            db.collection("users").document(uid).collection("addresses").document(addressId!!)

        val newAddress = Address(
            id = ref.id,
            label = label,
            recipientName = name,
            phoneNumber = phone,
            fullAddress = fullAddr,
            details = details,
            notes = notes,
            latitude = tempLat,
            longitude = tempLng,
            isPrimary = cbPrimary.isChecked
        )

        // Simpan ke Firestore
        val saveTask = {
            ref.set(newAddress)
                .addOnSuccessListener {
                    Toast.makeText(context, "Alamat tersimpan!", Toast.LENGTH_SHORT).show()

                    // Setelah simpan: Kembali ke Payment (Pop 2 kali: Form -> Peta -> Payment)
                    // Atau gunakan popBackStack ke ID fragment tujuan jika tumpukan dalam
                    // Untuk saat ini standar popBackStack() cukup jika flow: Payment -> Peta -> Form
                    // Agar user tidak kembali ke Peta setelah simpan, kita bisa pop ke PaymentFragment.

                    // Cara aman: popBackStack() sekali.
                    // Jika stack: Payment -> Peta -> Form. Pop -> Peta. (User harus back lagi).
                    // Solusi: Navigasi Explicit kembali ke Payment atau popInclusive.

                    findNavController().popBackStack(R.id.paymentFragment, false)
                    // Pastikan ID fragment payment Anda adalah 'paymentFragment' di nav_graph
                    // Jika ragu, gunakan findNavController().popBackStack() saja.
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal menyimpan: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        if (cbPrimary.isChecked) {
            resetOtherPrimaryAddresses(uid, db) {
                saveTask()
            }
        } else {
            saveTask()
        }
    }

    private fun resetOtherPrimaryAddresses(uid: String, db: FirebaseFirestore, onComplete: () -> Unit) {
        db.collection("users").document(uid).collection("addresses")
            .whereEqualTo("isPrimary", true)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (doc in documents) {
                    if (doc.id != addressId) {
                        batch.update(doc.reference, "isPrimary", false)
                    }
                }
                batch.commit().addOnCompleteListener { onComplete() }
            }
            .addOnFailureListener { onComplete() }
    }
}