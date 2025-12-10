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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.R
import com.example.map_umkm.databinding.FragmentReferralBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReferralFragment : Fragment() {

    // Menggunakan View Binding
    private var _binding: FragmentReferralBinding? = null
    private val binding get() = _binding!!

    // Firebase
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

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Menambahkan try-catch di sekitar inisialisasi listener untuk mencegah crash karena Missing View ID
        try {
            loadReferralCodeFromFirestore()
            setupListeners()
        } catch (e: NullPointerException) {
            // Ini menangkap jika salah satu View ID (misal: btnCopyCode) tidak ada di XML
            Log.e(TAG, "FATAL: View ID tidak ditemukan saat setup: ${e.message}", e)
            Snackbar.make(view, "Kesalahan tampilan. Harap laporkan bug ini.", Snackbar.LENGTH_LONG).show()
            // Nonaktifkan tombol untuk mencegah crash lebih lanjut
            binding.btnShareReferral.isEnabled = false
        } catch (e: Exception) {
            Log.e(TAG, "Error umum saat inisialisasi: ${e.message}", e)
            Snackbar.make(view, "Error saat inisialisasi fragment.", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        // Tombol Back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol Info Referral
        binding.btnReferralInfo.setOnClickListener {
            Snackbar.make(requireView(), "Informasi Referral...", Snackbar.LENGTH_SHORT).show()
        }

        // Tombol Copy Code
        binding.btnCopyCode.setOnClickListener {
            val codeToCopy = binding.tvReferralCode.text.toString()
            if (codeToCopy.length > 5 && codeToCopy != "Gagal memuat kode." && codeToCopy != "Silakan Login Ulang") {
                copyCodeToClipboard(codeToCopy)
            } else {
                Snackbar.make(requireView(), "Kode belum siap untuk disalin.", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Tombol Share
        binding.btnShareReferral.setOnClickListener {
            val codeToShare = binding.tvReferralCode.text.toString()
            if (codeToShare.length > 5 && codeToShare != "Gagal memuat kode." && codeToShare != "Silakan Login Ulang") {
                shareReferralCode(codeToShare)
            } else {
                Snackbar.make(requireView(), "Kode belum siap dibagikan.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Mengambil ownReferralCode dari dokumen user di Firestore.
     * Menangani kasus jika user lama belum punya field ini (Self-correction).
     */
    private fun loadReferralCodeFromFirestore() {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid.isNullOrEmpty()) {
            // Kasus 1: User belum login
            binding.tvReferralCode.text = "Silakan Login Ulang"
            binding.btnShareReferral.isEnabled = false
            binding.btnCopyCode.isEnabled = false
            return
        }

        // Kasus 2: User login, ambil data dari Firestore
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                // Asumsi field di Firestore adalah 'ownReferralCode'
                val code = document.getString("ownReferralCode")

                if (!code.isNullOrEmpty()) {
                    // Kasus 2a: Kode ditemukan dan valid
                    binding.tvReferralCode.text = code
                    binding.btnShareReferral.isEnabled = true
                    binding.btnCopyCode.isEnabled = true
                } else {
                    // Kasus 2b: Kode hilang (User lama). Lakukan Self-Correction.
                    val newCode = uid.substring(0, 6).uppercase()
                    binding.tvReferralCode.text = newCode

                    // Simpan kode yang baru dibuat ini kembali ke Firestore (Update)
                    db.collection("users").document(uid).update("ownReferralCode", newCode)
                        .addOnSuccessListener {
                            Log.d(TAG, "Kode referral otomatis dibuat dan disimpan: $newCode")
                            binding.btnShareReferral.isEnabled = true
                            binding.btnCopyCode.isEnabled = true
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Gagal menyimpan kode yang baru dibuat: ${e.message}", e)
                            binding.tvReferralCode.text = "Error update kode."
                        }
                }
            }
            .addOnFailureListener { e ->
                // Kasus 3: Error koneksi atau dokumen tidak dapat diakses
                Log.e(TAG, "Gagal koneksi ke Firestore: ${e.message}", e)
                binding.tvReferralCode.text = "Error koneksi."
                binding.btnShareReferral.isEnabled = false
                binding.btnCopyCode.isEnabled = false
            }
    }

    /**
     * Menyalin kode ke clipboard perangkat.
     */
    private fun copyCodeToClipboard(code: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Kode referral '$code' disalin!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Membuka Intent untuk berbagi kode referral.
     */
    private fun shareReferralCode(code: String) {
        val message = "Dapatkan diskon spesial! ✨ Gunakan kode referral unikku: $code " +
                "saat mendaftar atau checkout di aplikasi TUKU untuk promo menarik! " +
                "Download sekarang: [LINK_APLIKASI_ANDA_DI_PLAYSTORE_ATAU_APPSTORE]"

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