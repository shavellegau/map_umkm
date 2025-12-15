package com.example.map_umkm

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.map_umkm.adapter.BannerAdapter
import com.example.map_umkm.databinding.FragmentHomeBinding
import com.example.map_umkm.model.MenuData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.*

//private val .uid: Any

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var userListener: ListenerRegistration? = null


    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var updateRunnable: Runnable? = null

    private val pilihCabangLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Muat lokasi jika PilihCabangActivity sukses (RESULT_OK)
        if (result.resultCode == Activity.RESULT_OK) {
            loadSavedLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayUserGreeting()
        setupBannerCarousel()
        loadNewestMenuFromJson()

        setupListeners()

        loadSavedLocation()

        getVoucherCount()
        getUnreadNotificationCount()
    }

    override fun onResume() {
        super.onResume()
        loadSavedLocation()
        getVoucherCount()
        getUnreadNotificationCount()

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        binding.tvUserGreeting.text = "Hi, ${prefs.getString("userName", "User")}!"
    }

    private fun loadSavedLocation() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val savedBranchName = prefs.getString("selectedBranchName", "Pilih Cabang")
        binding.tvCurrentLocation.text = savedBranchName
    }

    private fun displayUserGreeting() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        userListener = db.collection("users")
            .document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e("HomeFragment", "listener error: ${error.message}")
                    return@addSnapshotListener
                }
                val newName = doc?.getString("name") ?: "Pengguna"
                if (_binding != null) {
                    binding.tvUserGreeting.text = "Hi, $newName!"
                }
            }
    }


    private fun getVoucherCount() {
        val db = FirebaseFirestore.getInstance()

        db.collection("vouchers")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                updateVoucherCountUI(count)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Gagal menghitung voucher publik: ${e.message}")
                updateVoucherCountUI(0)
            }
    }


    private fun updateVoucherCountUI(count: Int) {
        if (_binding != null) {
            // Tampilkan jumlah voucher dengan label "Voucher"
            binding.tvVoucherCount.text = "$count Voucher"
        }
    }

    private fun getUnreadNotificationCount() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("userEmail", null)

        if (userEmail == null) return

        val db = FirebaseFirestore.getInstance()

        db.collection("notifications")
            .whereEqualTo("userEmail", userEmail)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                updateNotificationBadge(count)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Gagal mengambil jumlah notifikasi: ${e.message}")
                updateNotificationBadge(0)
            }
    }


    private fun updateNotificationBadge(count: Int) {
        if (_binding != null) {
            if (count > 0) {
                // Tampilkan angka, batasi hingga "9+"
                binding.tvNotificationCount.text = if (count > 9) "9+" else count.toString()
                binding.tvNotificationCount.visibility = View.VISIBLE
            } else {
                binding.tvNotificationCount.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.locationCard.setOnClickListener {
            val intent = Intent(requireActivity(), PilihCabangActivity::class.java)
            pilihCabangLauncher.launch(intent)
        }
        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_notificationFragment)
        }

        binding.btnTukuPoint.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_tetanggaTukuFragment)
        }

        binding.ivTukuPoint.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_tukuPointFragment)
        }

        binding.referralCard.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_referralFragment)
        }

        binding.voucherCard.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_voucherSayaFragment)
        }

        binding.tukuCareCard.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_bantuanFragment)
        }

        binding.ivKodeRedeem.setOnClickListener {
            showRedeemDialog()
        }

        val changeTabToMenu = {
            if (activity is MainActivity) {
                (activity as MainActivity).bottomNavigationView.selectedItemId = R.id.nav_cart
            }
        }

        binding.takeAwayCard.setOnClickListener {
            changeTabToMenu()
        }

        binding.deliveryCard.setOnClickListener {
            changeTabToMenu()
        }
    }

    private fun showRedeemDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_redeem_code)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etCode = dialog.findViewById<EditText>(R.id.et_redeem_code)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel_redeem)
        val btnApply = dialog.findViewById<Button>(R.id.btn_apply_redeem)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.isNotEmpty()) {
                Toast.makeText(requireContext(), "Kode '$code' sedang diproses...", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Kode tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupBannerCarousel() {
        val bannerImages = listOf(R.drawable.banner_tuku_hut, R.drawable.tuku_banner, R.drawable.banner_tuku_mrt)
        val bannerAdapter = BannerAdapter(bannerImages)
        binding.bannerViewPager.adapter = bannerAdapter
        var currentPage = 0
        updateRunnable = Runnable {
            if (isAdded && _binding != null) {
                currentPage = (currentPage + 1) % bannerImages.size
                binding.bannerViewPager.setCurrentItem(currentPage, true)
            }
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                updateRunnable?.let { handler.post(it) }
            }
        }, 3000, 3000)
    }

    private fun loadNewestMenuFromJson() {
        try {
            val inputStream = context?.assets?.open("menu_items.json")
            val reader = InputStreamReader(inputStream)
            val menuDataType = object : TypeToken<MenuData>() {}.type
            val menuData: MenuData = Gson().fromJson(reader, menuDataType)

            // Mengambil item dengan createdAt terbaru dari JSON lokal
            val newestMenuItem = menuData.menu.filter { !it.createdAt.isNullOrEmpty() }.maxByOrNull { it.createdAt!! }

            if (newestMenuItem != null) {
                binding.newestMenuCard.visibility = View.VISIBLE
                binding.tvNewestMenuName.text = newestMenuItem.name
                val priceHot = newestMenuItem.price_hot?.let { "Hot: Rp $it" } ?: ""
                val priceIced = newestMenuItem.price_iced?.let { "Iced: Rp $it" } ?: ""
                binding.tvNewestMenuPrice.text = listOf(priceHot, priceIced).filter { it.isNotEmpty() }.joinToString(" / ")

                newestMenuItem.image?.let { imageName ->
                    val context = requireContext()
                    val imageResId = context.resources.getIdentifier(
                        imageName,
                        "drawable",
                        context.packageName
                    )

                    val source = if (imageResId != 0) imageResId else R.drawable.placeholder_image

                    Glide.with(this).load(source).into(binding.ivNewestMenuImage)
                }

                binding.newestMenuCard.setOnClickListener {
                    Toast.makeText(requireContext(), "Clicked on ${newestMenuItem.name}", Toast.LENGTH_SHORT).show()
                }

            } else {
                binding.newestMenuCard.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error loading JSON: ${e.message}")
            binding.newestMenuCard.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        userListener = null

        timer?.cancel()
        timer = null
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        _binding = null
    }
}