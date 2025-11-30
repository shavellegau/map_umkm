package com.example.map_umkm.ui.referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.R
import com.example.map_umkm.databinding.FragmentReferralBinding // Import View Binding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ReferralFragment : Fragment() {

    private var _binding: FragmentReferralBinding? = null
    private val binding get() = _binding!! // MSenggunakan View Binding

    private lateinit var auth: FirebaseAuth

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

        loadReferralCode()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnReferralInfo.setOnClickListener {
            // Aksi ketika tombol "Referral Kamu" / info diklik
            // Misalnya: tampilkan dialog berisi syarat dan ketentuan referral, atau navigasi ke halaman lain
            Snackbar.make(requireView(), "Informasi Referral...", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnCopyCode.setOnClickListener {
            val codeToCopy = binding.tvReferralCode.text.toString()
            if (codeToCopy.length > 5 && codeToCopy != "Gagal memuat kode.") {
                copyCodeToClipboard(codeToCopy)
            } else {
                Snackbar.make(requireView(), "Kode belum siap untuk disalin.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnShareReferral.setOnClickListener {
            val codeToShare = binding.tvReferralCode.text.toString()
            if (codeToShare.length > 5 && codeToShare != "Gagal memuat kode.") {
                shareReferralCode(codeToShare)
            } else {
                Snackbar.make(requireView(), "Kode belum siap dibagikan.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadReferralCode() {
        val user = auth.currentUser
        val fullUid = user?.uid

        if (!fullUid.isNullOrEmpty()) {
            val referralCode = fullUid.substring(0, 6).uppercase() // Menggunakan 6 karakter agar lebih singkat
            binding.tvReferralCode.text = referralCode
            binding.btnShareReferral.isEnabled = true
            binding.btnCopyCode.isEnabled = true
        } else {
            binding.tvReferralCode.text = "Gagal memuat kode."
            binding.btnShareReferral.isEnabled = false
            binding.btnCopyCode.isEnabled = false
            Snackbar.make(requireView(), "Gagal mendapatkan User ID. Silakan cek login Anda.", Snackbar.LENGTH_LONG).show()
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