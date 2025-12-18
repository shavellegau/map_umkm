package com.example.map_umkm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var btnPilihPeta: Button
    private lateinit var cbPrimary: CheckBox
    private lateinit var btnSave: Button
    private lateinit var toolbar: MaterialToolbar
    private var tempLatitude: Double? = null
    private var tempLongitude: Double? = null
    private var addressId: String? = null 

    private val mapResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        
        if (!isAdded) return@registerForActivityResult

        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val lat = data.getDoubleExtra("LATITUDE", 0.0)
                    val lng = data.getDoubleExtra("LONGITUDE", 0.0)
                    val addressStr = data.getStringExtra("ALAMAT") ?: ""

                    
                    tempLatitude = lat
                    tempLongitude = lng
                    
                    if (::etFullAddress.isInitialized) {
                        etFullAddress.setText(addressStr)
                        
                        if (etFullAddress.text.isNotEmpty()) {
                            etFullAddress.setSelection(etFullAddress.text.length)
                        }
                    } else {
                        Log.w("AddAddress", "View belum siap, alamat disimpan di variabel saja.")
                    }

                    Toast.makeText(context, "Lokasi berhasil dipilih!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            
            Toast.makeText(context, "Gagal memuat lokasi, silakan ketik manual.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        arguments?.let {
            addressId = it.getString("addressId") 
        }

        
        etLabel = view.findViewById(R.id.etAddressLabel)
        etName = view.findViewById(R.id.etRecipientName)
        etPhone = view.findViewById(R.id.etPhoneNumber)
        etFullAddress = view.findViewById(R.id.etFullAddress)
        btnPilihPeta = view.findViewById(R.id.btn_pilih_di_peta)
        cbPrimary = view.findViewById(R.id.cbSetAsPrimary)
        btnSave = view.findViewById(R.id.btnSaveAddress)
        toolbar = view.findViewById(R.id.toolbar)

        
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        
        btnPilihPeta.setOnClickListener {
            val intent = Intent(requireContext(), PetaPilihLokasiActivity::class.java)
            mapResultLauncher.launch(intent)
        }

        
        btnSave.setOnClickListener {
            saveAddress()
        }
    }

    private fun saveAddress() {
        val label = etLabel.text.toString().trim()
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val finalAddressText = etFullAddress.text.toString().trim()
        val isPrimary = cbPrimary.isChecked

        if (label.isEmpty() || name.isEmpty() || phone.isEmpty() || finalAddressText.isEmpty()) {
            Toast.makeText(context, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Sesi habis, login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        
        val db = FirebaseFirestore.getInstance()
        val docRef = if (addressId.isNullOrEmpty()) {
            db.collection("users").document(uid).collection("addresses").document()
        } else {
            db.collection("users").document(uid).collection("addresses").document(addressId!!)
        }

        val newAddress = Address(
            id = docRef.id,
            uid = uid,
            label = label,
            recipientName = name,
            phoneNumber = phone,
            fullAddress = finalAddressText,
            isPrimary = isPrimary,
            latitude = tempLatitude,
            longitude = tempLongitude
        )

        
        docRef.set(newAddress)
            .addOnSuccessListener {
                Toast.makeText(context, "Alamat berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() 
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}