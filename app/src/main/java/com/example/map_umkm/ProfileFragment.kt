package com.example.map_umkm.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import com.example.map_umkm.R

// tambahkan ini untuk activity-activity mu
import com.example.map_umkm.PesananActivity
import com.example.map_umkm.WishlistActivity
import com.example.map_umkm.UlasanActivity
import com.example.map_umkm.VoucherActivity
import com.example.map_umkm.PengaturanAkunActivity
import com.example.map_umkm.AlamatActivity
import com.example.map_umkm.BantuanActivity

class ProfileFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnEditProfile: ImageButton
    private lateinit var txtName: TextView
    private lateinit var btnLogout: Button

    // Launcher untuk pilih foto dari galeri
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

        // Binding view
        imgProfile = root.findViewById(R.id.imgProfile)
        btnEditProfile = root.findViewById(R.id.btnEditProfile)
        txtName = root.findViewById(R.id.txtName)
        btnLogout = root.findViewById(R.id.btnLogout)

        // Edit profil
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // Logout
        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(requireContext(), LoginActivity::class.java))
            // requireActivity().finish()
        }

        // Navigasi menu
        root.findViewById<View>(R.id.cardPesanan).setOnClickListener {
            openActivity(PesananActivity::class.java)
        }
        root.findViewById<View>(R.id.cardWishlist).setOnClickListener {
            openActivity(WishlistActivity::class.java)
        }
        root.findViewById<View>(R.id.cardUlasan).setOnClickListener {
            openActivity(UlasanActivity::class.java)
        }
        root.findViewById<View>(R.id.cardVoucher).setOnClickListener {
            openActivity(VoucherActivity::class.java)
        }
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


    // Fungsi untuk pindah Activity
    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(requireContext(), activityClass)
        startActivity(intent)
    }

    // Dialog untuk edit nama + foto profil
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
