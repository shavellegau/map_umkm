package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
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
    private var fromPayment: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_edit_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        fromPayment = arguments?.getBoolean("from_payment") ?: false
        val hasilAlamat = arguments?.getString("hasil_alamat")
        val hasilLat = arguments?.getDouble("hasil_lat")
        val hasilLng = arguments?.getDouble("hasil_lng")

        if(hasilAlamat != null && hasilLat != null && hasilLng != null){
            etFullAddress.setText(hasilAlamat)
            tempLat = hasilLat
            tempLng = hasilLng
        }


        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        setFragmentResultListener("requestKey_lokasi") { _, bundle ->
            val alamat = bundle.getString("hasil_alamat")
            val lat = bundle.getDouble("hasil_lat")
            val lng = bundle.getDouble("hasil_lng")
            if (alamat != null) {
                etFullAddress.setText(alamat)
                tempLat = lat
                tempLng = lng
            }
        }

        btnPilihPeta.setOnClickListener {
            val bundle = bundleOf("from_payment" to fromPayment)
            findNavController().navigate(R.id.action_addEditAddressFragment_to_pilihLokasiFragment, bundle)
        }

        btnSave.setOnClickListener { saveAddress() }
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
        val ref = db.collection("users").document(uid).collection("addresses").document() // Buat ID baru

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

        ref.set(newAddress)
            .addOnSuccessListener {
                Toast.makeText(context, "Alamat berhasil disimpan!", Toast.LENGTH_SHORT).show()

                if (fromPayment) {
                    val resultBundle = bundleOf("data_alamat" to newAddress)
                    setFragmentResult("request_alamat", resultBundle)
                    findNavController().popBackStack(R.id.paymentFragment, false)
                } else {
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
