package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import android.widget.Button

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser!!.uid

        // 1 ===> AMBIL DARI FIREBASE AUTH DULU
        binding.txtName.text = auth.currentUser?.displayName ?: "User"

        // 2 ===> LISTENER REALTIME FIRESTORE
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .addSnapshotListener { doc, _ ->

                val name = doc?.getString("name")
                if (name != null) {
                    binding.txtName.text = name

                    // sync ke SharedPref
                    val ed = prefs.edit()
                    ed.putString("userName", name)
                    ed.apply()
                }
            }

        // tampil email
        binding.tvMember.text = prefs.getString("userEmail", "email@user.com")

        // click navigations
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

        binding.menuBantuan.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_bantuanFragment)
        }

        binding.menuAlamat.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_alamatFragment)
        }

        binding.menuPengaturanAkun.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_pengaturanAkunFragment)
        }

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
