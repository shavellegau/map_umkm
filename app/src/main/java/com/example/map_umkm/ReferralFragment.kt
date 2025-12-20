package com.example.map_umkm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.databinding.FragmentReferralBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReferralFragment : Fragment() {

    private var _binding: FragmentReferralBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val TAG = "ReferralFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReferralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        try {
            loadReferralCodeFromFirestore()
            setupListeners()
        } catch (e: NullPointerException) {
            Log.e(TAG, "FATAL: View ID tidak ditemukan saat setup: ${e.message}", e)
            Snackbar.make(view, "Kesalahan tampilan. Harap laporkan bug ini.", Snackbar.LENGTH_LONG).show()
            binding.btnShareReferral.isEnabled = false
        } catch (e: Exception) {
            Log.e(TAG, "Error umum saat inisialisasi: ${e.message}", e)
            Snackbar.make(view, "Error saat inisialisasi fragment.", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Fungsi Tukar Kode (Menampilkan Dialog Input)
        binding.btnReferralInfo.setOnClickListener {
            showRedeemDialog()
        }

        binding.btnCopyCode.setOnClickListener {
            val codeToCopy = binding.tvReferralCode.text.toString()
            if (codeToCopy.length > 5 && codeToCopy != "Gagal memuat kode." && codeToCopy != "Silakan Login Ulang") {
                copyCodeToClipboard(codeToCopy)
            } else {
                Snackbar.make(requireView(), "Kode belum siap untuk disalin.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnShareReferral.setOnClickListener {
            val codeToShare = binding.tvReferralCode.text.toString()
            if (codeToShare.length > 5 && codeToShare != "Gagal memuat kode." && codeToShare != "Silakan Login Ulang") {
                shareReferralCode(codeToShare)
            } else {
                Snackbar.make(requireView(), "Kode belum siap dibagikan.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRedeemDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Tukarkan Kode Referral")

        val input = EditText(requireContext())
        input.hint = "Masukkan kode teman (contoh: YSWMED)"
        builder.setView(input)

        builder.setPositiveButton("Tukarkan") { _, _ ->
            val code = input.text.toString().trim().uppercase()
            if (code.isNotEmpty()) {
                redeemReferralCode(code)
            } else {
                Toast.makeText(requireContext(), "Kode tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun redeemReferralCode(inputCode: String) {
        val currentUserUid = auth.currentUser?.uid ?: return

        Toast.makeText(requireContext(), "Mencari kode: $inputCode", Toast.LENGTH_SHORT).show()

        db.collection("users")
            .whereEqualTo("ownReferralCode", inputCode)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "GAGAL: Kode $inputCode tidak ditemukan!", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val referrerDoc = documents.documents[0]
                val referrerUid = referrerDoc.id

                if (referrerUid == currentUserUid) {
                    Toast.makeText(requireContext(), "Tidak bisa pakai kode sendiri", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("users").document(currentUserUid).get()
                    .addOnSuccessListener { myDoc ->
                        if (myDoc.contains("referredBy")) {
                            Toast.makeText(requireContext(), "Anda sudah pernah mengklaim kode!", Toast.LENGTH_SHORT).show()
                        } else {
                            executeReferralReward(currentUserUid, referrerUid, inputCode)
                        }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error Koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun executeReferralReward(myUid: String, referrerUid: String, code: String) {
        val db = FirebaseFirestore.getInstance()
        val voucherData = hashMapOf(
            "title" to "Voucher Referral 50%",
            "desc" to "Hadiah dari kode $code",
            "code" to "REF-$code",
            "isActive" to true,
            "createdAt" to System.currentTimeMillis()
        )

        db.runTransaction { transaction ->
            val myRef = db.collection("users").document(myUid)
            val referrerRef = db.collection("users").document(referrerUid)

            transaction.update(myRef, "referredBy", code)

            transaction.set(myRef.collection("vouchers").document(), voucherData)
            transaction.set(referrerRef.collection("vouchers").document(), voucherData)
            null
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "Berhasil! Voucher ditambahkan.", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { e ->
            Log.e("REFERRAL_ERROR", "Transaksi Gagal: ${e.message}")
            Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadReferralCodeFromFirestore() {
        val user = auth.currentUser
        val uid = user?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val code = document.getString("ownReferralCode")
                if (!code.isNullOrEmpty()) {
                    binding.tvReferralCode.text = code
                } else {
                    // Membuat kode baru jika belum ada di dokumen user
                    val newCode = uid.substring(0, 6).uppercase()
                    binding.tvReferralCode.text = newCode
                    db.collection("users").document(uid).update("ownReferralCode", newCode)
                }
            }
            .addOnFailureListener {
                binding.tvReferralCode.text = "Error koneksi."
            }
    }

    private fun copyCodeToClipboard(code: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Kode referral '$code' disalin!", Toast.LENGTH_SHORT).show()
    }

    private fun shareReferralCode(code: String) {
        val message = "Dapatkan diskon spesial! ✨ Gunakan kode referral unikku: $code " +
                "saat mendaftar atau checkout di aplikasi TUKU untuk promo menarik!"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Bagikan Kode Referral Via:"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}