package com.example.map_umkm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar // Import Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class AdminVoucherFragment : Fragment() {

    private lateinit var etCode: EditText
    private lateinit var etTitle: EditText
    private lateinit var etDisc: EditText
    private lateinit var etMin: EditText
    private lateinit var etExpiry: EditText
    private lateinit var btnSave: Button
    private lateinit var toolbar: Toolbar // Tambahan untuk Toolbar

    // Service Notifikasi
    private lateinit var fcmService: FCMService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Pastikan nama layout sesuai dengan file XML kamu
        val view = inflater.inflate(R.layout.fragment_admin_voucher, container, false)

        fcmService = FCMService(requireContext())

        // Binding Views (Sesuai ID di XML kamu)
        etCode = view.findViewById(R.id.et_voucher_code)
        etTitle = view.findViewById(R.id.et_voucher_title)
        etDisc = view.findViewById(R.id.et_voucher_disc)
        etMin = view.findViewById(R.id.et_voucher_min)
        etExpiry = view.findViewById(R.id.et_voucher_expiry)
        btnSave = view.findViewById(R.id.btn_save_voucher)

        // Binding Toolbar (Agar tombol back di atas berfungsi)
        toolbar = view.findViewById(R.id.toolbar_add_voucher)
        toolbar.setNavigationOnClickListener {
            // Perintah untuk menutup fragment ini dan kembali ke dashboard
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            validateAndSave()
        }

        return view
    }

    private fun validateAndSave() {
        val code = etCode.text.toString().uppercase().trim()
        val title = etTitle.text.toString().trim()
        val discStr = etDisc.text.toString().trim()
        val minStr = etMin.text.toString().trim()
        val expiry = etExpiry.text.toString().trim()

        if (code.isEmpty() || title.isEmpty() || discStr.isEmpty() || minStr.isEmpty() || expiry.isEmpty()) {
            Toast.makeText(context, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val disc = discStr.toDoubleOrNull() ?: 0.0
        val min = minStr.toDoubleOrNull() ?: 0.0

        saveToFirestore(code, title, disc, min, expiry)
    }

    private fun saveToFirestore(code: String, title: String, disc: Double, min: Double, expiry: String) {
        val db = FirebaseFirestore.getInstance()

        val voucherData = hashMapOf(
            "code" to code,
            "title" to title,
            "discountAmount" to disc,
            "minPurchase" to min,
            "expiryDate" to expiry,
            "isActive" to true,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("vouchers").document(code)
            .set(voucherData)
            .addOnSuccessListener {
                Toast.makeText(context, "Voucher Berhasil Dibuat!", Toast.LENGTH_SHORT).show()

                // Reset Input
                etCode.setText("")
                etTitle.setText("")
                etDisc.setText("")
                etMin.setText("")
                etExpiry.setText("")

                // Kirim Notif
                sendPromoNotification(code, title, disc)

                // Opsional: Langsung tutup halaman setelah simpan
                // parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendPromoNotification(code: String, title: String, disc: Double) {
        fcmService.sendNotification(
            "promo",
            "Voucher Baru: $title",
            "Gunakan kode $code untuk potongan Rp ${disc.toInt()}!"
        )
    }
}