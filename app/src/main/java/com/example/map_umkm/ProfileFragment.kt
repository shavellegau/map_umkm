package com.example.map_umkm

import android.content.Context
import android.content.Intent
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController  // ← penting untuk navigasi antar fragment

class ProfileFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnEditProfile: ImageButton
    private lateinit var txtName: TextView
    private lateinit var btnLogout: Button

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                imgProfile.setImageURI(imageUri)
                Toast.makeText(requireContext(), "Foto profil berhasil diganti", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        imgProfile = root.findViewById(R.id.imgProfile)
        btnEditProfile = root.findViewById(R.id.btnEditProfile)
        txtName = root.findViewById(R.id.txtName)
        btnLogout = root.findViewById(R.id.btnLogout)

        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        btnLogout.setOnClickListener {
            // --- Hapus session SharedPreferences
            val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // --- Kembali ke LoginActivity dan clear backstack supaya user tidak bisa "Back" masuk lagi
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // --- Navigasi antar fragment lewat NavGraph
        root.findViewById<View>(R.id.cardPesanan).setOnClickListener {
            findNavController().navigate(R.id.pesananSayaFragment)
        }

        root.findViewById<View>(R.id.cardWishlist).setOnClickListener {
            findNavController().navigate(R.id.wishlistFragment)
        }

        root.findViewById<View>(R.id.cardUlasan).setOnClickListener {
            findNavController().navigate(R.id.riwayatPoinFragment)
        }

        root.findViewById<View>(R.id.cardVoucher).setOnClickListener {
            findNavController().navigate(R.id.voucherSayaFragment)
        }

        // --- Menu lain yang masih activity
        root.findViewById<View>(R.id.menuPengaturanAkun).setOnClickListener {
            openActivity(PengaturanAkunActivity::class.java)
        }
        root.findViewById<View>(R.id.menuAlamat).setOnClickListener {
            openActivity(AlamatActivity::class.java)
        }
        root.findViewById<View>(R.id.menuBantuan).setOnClickListener {
            openActivity(BantuanActivity::class.java)
        }

        return root
    }

    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(requireContext(), activityClass)
        startActivity(intent)
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val btnChangePhoto = dialogView.findViewById<Button>(R.id.btnChangePhoto)

        etName.setText(txtName.text)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Profil")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                txtName.text = etName.text.toString()
                Toast.makeText(requireContext(), "Nama berhasil diganti", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .create()

        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        dialog.show()
    }
}
