package com.example.map_umkm

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.map_umkm.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
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
        if (uri != null) uploadImageToStorage(uri)
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && tempImageUri != null) {
            uploadImageToStorage(tempImageUri!!)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
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
        binding.tvMemberPoints.text = "0 Pts"
        binding.tvMemberStatus.text = "Loading..."

        val currentPhotoUrl = currentUser.photoUrl
        if (currentPhotoUrl != null) {
            Glide.with(this).load(currentPhotoUrl).into(ivProfile)
        }

        ivProfile.setOnClickListener {
            showImagePickerOptions()
        }

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .addSnapshotListener { doc, _ ->
                if (_binding == null || !isAdded) return@addSnapshotListener
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name")
                    if (name != null) {
                        binding.txtName.text = name
                        prefs.edit().putString("userName", name).apply()
                    }

                    val photoUrl = doc.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(photoUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .into(ivProfile)
                    }

                    val points = doc.getLong("tukuPoints") ?: 0
                    val formattedPoints = NumberFormat.getInstance(Locale("in", "ID")).format(points)
                    binding.tvMemberPoints.text = "$formattedPoints Pts"

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

    private fun showImagePickerOptions() {
        val options = arrayOf("Ambil Foto (Kamera)", "Pilih dari Galeri")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ganti Foto Profil")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermissionAndOpen() 
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                
                openCamera()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val tmpFile = File.createTempFile("tmp_photo", ".jpg", requireContext().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }

            val authority = "${requireContext().packageName}.fileprovider"

            tempImageUri = FileProvider.getUriForFile(
                requireContext(),
                authority,
                tmpFile
            )

            cameraLauncher.launch(tempImageUri)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImageToStorage(fileUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.progressBarProfile.visibility = View.VISIBLE
        binding.ivProfile.alpha = 0.5f
        binding.ivProfile.isEnabled = false

        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    if (_binding != null) {
                        saveUrlToDatabase(uri.toString())
                    }
                }
            }
            .addOnFailureListener { e ->
                _binding?.let {
                    it.progressBarProfile.visibility = View.GONE
                    it.ivProfile.alpha = 1.0f
                    it.ivProfile.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUrlToDatabase(imageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(imageUrl))
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                db.collection("users").document(user.uid)
                    .update("photoUrl", imageUrl)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        ivProfile.alpha = 1.0f
                        ivProfile.isEnabled = true
                        Toast.makeText(context, "Foto Profil Berhasil!", Toast.LENGTH_SHORT).show()
                        Glide.with(this).load(imageUrl).into(ivProfile)
                    }
            }
        }
    }

    private fun setupNavigation() {
        binding.cardExp.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_tetanggaTukuFragment) }
        binding.cardTukuPoint.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_tukuPointFragment) }
        binding.cardWishlist.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_wishlistFragment) }
        binding.cardVoucher.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_voucherSayaFragment) }
        binding.menuBantuan.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_bantuanFragment) }
        binding.menuAlamat.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_alamatFragment) }
        binding.menuPengaturanAkun.setOnClickListener { findNavController().navigate(R.id.action_nav_profile_to_pengaturanAkunFragment) }
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }
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