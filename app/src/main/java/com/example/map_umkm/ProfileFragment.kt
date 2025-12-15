package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            logout()
            return
        }

        val uid = currentUser.uid

        // 1. TAMPILKAN DATA SEMENTARA (CACHE/AUTH) AGAR TIDAK KOSONG
        binding.txtName.text = currentUser.displayName ?: prefs.getString("userName", "Pengguna Tuku")
        binding.tvEmail.text = currentUser.email ?: prefs.getString("userEmail", "-")

        // Placeholder Poin
        binding.tvMemberPoints.text = "0 Pts"
        binding.tvMemberStatus.text = "New Member"

        // 2. LISTENER REALTIME FIRESTORE (NAMA & POIN)
        // Kita pakai satu listener saja untuk mengambil Nama DAN Poin sekaligus
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .addSnapshotListener { doc, error ->

                // Cek validitas fragment & error
                if (_binding == null || !isAdded) return@addSnapshotListener
                if (error != null) return@addSnapshotListener

                if (doc != null && doc.exists()) {

                    // A. UPDATE NAMA
                    val name = doc.getString("name")
                    if (name != null) {
                        binding.txtName.text = name
                        prefs.edit().putString("userName", name).apply()
                    }

                    // B. UPDATE POIN (REALTIME) 🔥
                    val points = doc.getLong("tukuPoints") ?: 0

                    // Format angka (contoh: 5000 -> 5.000)
                    val formattedPoints = NumberFormat.getInstance(Locale("in", "ID")).format(points)
                    binding.tvMemberPoints.text = "$formattedPoints Pts"

                    // C. UPDATE STATUS MEMBER (LOGIKA SEDERHANA)
                    // Contoh: > 5000 jadi Gold, sisanya Silver
                    val status = when {
                        points >= 5000 -> "Gold Member"
                        points >= 1000 -> "Silver Member"
                        else -> "Bronze Member"
                    }
                    binding.tvMemberStatus.text = status
                }
            }

        // 3. NAVIGASI MENU
        setupNavigation()
    }

    private fun setupNavigation() {
        // Grid Menu (Aktivitas)
        binding.cardPesanan.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_historyOrdersFragment)
        }

        binding.cardTukuPoint.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_tukuPointFragment)
        }

        binding.cardWishlist.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_wishlistFragment)
        }

        binding.cardVoucher.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_voucherSayaFragment)
        }

        // List Menu (Pengaturan)
        binding.menuBantuan.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_bantuanFragment)
        }

        binding.menuAlamat.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_alamatFragment)
        }

        binding.menuPengaturanAkun.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_pengaturanAkunFragment)
        }

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirm, null)
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnLogout = dialogView.findViewById<Button>(R.id.btnLogout)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnLogout.setOnClickListener {
            dialog.dismiss()
            logout()
        }

        dialog.show()
    }

    private fun logout() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        FirebaseAuth.getInstance().signOut()

        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}