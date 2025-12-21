package com.example.map_umkm

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.map_umkm.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var ivProfile: ImageView
    private lateinit var progressBar: ProgressBar

    private var tempImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) saveImageToLocal(uri)
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && tempImageUri != null) {
            saveImageToLocal(tempImageUri!!)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) openCamera() else Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProfile = view.findViewById(R.id.ivProfile)
        progressBar = view.findViewById(R.id.progressBarProfile)

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            logout()
            return
        }

        val uid = currentUser.uid

        binding.txtName.text = currentUser.displayName ?: prefs.getString("userName", "Pengguna")
        binding.tvEmail.text = currentUser.email ?: prefs.getString("userEmail", "-")

        loadLocalProfileImage(uid, prefs)

        ivProfile.setOnClickListener {
            showImagePickerOptions()
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .addSnapshotListener { doc, _ ->
                if (_binding == null || !isAdded) return@addSnapshotListener
                if (doc != null && doc.exists()) {
                    binding.txtName.text = doc.getString("name") ?: binding.txtName.text
                    val points = doc.getLong("tukuPoints") ?: 0
                    binding.tvMemberPoints.text = "${NumberFormat.getInstance(Locale("in", "ID")).format(points)} Pts"

                    val currentXp = doc.getLong("currentXp")?.toInt() ?: 0
                    val tierName = when {
                        currentXp >= 1000 -> "Diamond Member"
                        currentXp >= 500 -> "Platinum Member"
                        currentXp >= 250 -> "Gold Member"
                        currentXp >= 100 -> "Silver Member"
                        else -> "Bronze Member"
                    }
                    binding.tvMemberStatus.text = "$tierName ($currentXp XP)"
                }
            }

        setupNavigation()
    }

    private fun setupNavigation() {
        binding.cardExp.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_nav_profile_to_tetanggaTukuFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Navigasi Exp Error", Toast.LENGTH_SHORT).show()
            }
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

        binding.cardMenuTukarPoin.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_tukuPointFragment)
        }

        binding.menuBantuan.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_bantuanFragment) }
        binding.menuAlamat.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_alamatFragment) }
        binding.menuPengaturanAkun.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_pengaturanAkunFragment) }
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun loadLocalProfileImage(uid: String, prefs: android.content.SharedPreferences) {
        val path = prefs.getString("local_profile_path_$uid", null)
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivProfile)
            }
        }
    }

    private fun saveImageToLocal(fileUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE
        ivProfile.alpha = 0.5f

        try {
            val inputStream = requireContext().contentResolver.openInputStream(fileUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val directory = File(requireContext().filesDir, "profile_images")
            if (!directory.exists()) directory.mkdirs()

            directory.listFiles()?.forEach { it.delete() }

            val newFile = File(directory, "profile_${uid}_${System.currentTimeMillis()}.jpg")
            val out = FileOutputStream(newFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()

            val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            prefs.edit().putString("local_profile_path_$uid", newFile.absolutePath).apply()

            progressBar.visibility = View.GONE
            ivProfile.alpha = 1.0f

            Glide.with(this)
                .load(newFile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivProfile)

            Toast.makeText(context, "Foto berhasil diganti!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            ivProfile.alpha = 1.0f
            Toast.makeText(context, "Gagal simpan gambar lokal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Ambil Foto (Kamera)", "Pilih dari Galeri")
        AlertDialog.Builder(requireContext())
            .setTitle("Ganti Foto Profil")
            .setItems(options) { _, which ->
                if (which == 0) checkCameraPermissionAndOpen() else galleryLauncher.launch("image/*")
            }.show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val tmpFile = File.createTempFile("tmp_photo", ".jpg", requireContext().cacheDir).apply { deleteOnExit() }
        tempImageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", tmpFile)
        cameraLauncher.launch(tempImageUri)
    }

    private fun showLogoutConfirmation() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout_confirm, null)
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            dialog.dismiss()
            logout()
        }
        dialog.show()
    }

    private fun logout() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}