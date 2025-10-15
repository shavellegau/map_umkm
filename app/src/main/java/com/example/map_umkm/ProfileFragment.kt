package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.databinding.FragmentProfileBinding // Menggunakan View Binding

class ProfileFragment : Fragment() {

    // Setup ViewBinding untuk menghindari kesalahan ID
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout menggunakan View Binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)

        // [FIXED] Menggunakan ID yang benar dari fragment_profile.xml melalui binding
        binding.txtName.text = prefs.getString("userName", "Nama Pengguna")
        // NOTE: ID untuk email tidak ada di layout, jadi saya tampilkan di tvMember untuk sementara
        binding.tvMember.text = prefs.getString("userEmail", "email@pengguna.com")

        // Setup Listeners menggunakan binding dengan ID yang benar
        binding.cardPesanan.setOnClickListener {
            // Navigasi ke pesanan saya. Asumsi ID action di nav_graph sudah benar
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

        binding.menuBantuan.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_bantuanFragment)
        }

//        // Listener untuk tombol/menu yang mengarah ke Activity
//        binding.btnEditProfile.setOnClickListener {
//            startActivity(Intent(activity, EditProfileActivity::class.java))
//        }

        binding.menuAlamat.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_alamatFragment)
        }

        binding.menuPengaturanAkun.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_pengaturanAkunFragment)
        }
        binding.menuPengaturanAkun.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_pengaturanAkunFragment)
        }



        // Listener untuk logout
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        // Inflate layout custom dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirm, null)
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Ambil view dari layout
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnLogout = dialogView.findViewById<Button>(R.id.btnLogout)

        // (Opsional) kalau mau ubah teks-nya dinamis
        tvTitle.text = "Logout Akun"
        tvMessage.text = "Apakah kamu yakin ingin keluar dari akun ini?"
        btnLogout.text = "Logout"

        // Tombol Batal
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Tombol Logout
        btnLogout.setOnClickListener {
            dialog.dismiss()
            logout() // Jalankan fungsi logout
        }

        dialog.show()
    }


    private fun logout() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }


    // Penting untuk mencegah memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
