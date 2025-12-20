package com.example.map_umkm

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.example.map_umkm.model.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.channels.FileChannel
import java.nio.FloatBuffer
import java.text.NumberFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private var userListener: ListenerRegistration? = null

    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var updateRunnable: Runnable? = null


    private var allRealMenus: List<MenuItem> = emptyList()

    private val pilihCabangLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
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


        loadMenuFromJson()


        setupUserDataListener()


        setupBannerCarousel()
        setupListeners()
        loadSavedLocation()
        getVoucherCount()
        getUnreadNotificationCount()


        setupBudgetOptimizer()
    }


    private fun setupUserDataListener() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        userListener = db.collection("users").document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("HomeFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {

                    val name = document.getString("name") ?: "Pengguna"
                    binding.tvUserGreeting.text = "Hi, $name!"


                    val points = document.getLong("tukuPoints")?.toInt() ?: 0
                    val formattedPoints = NumberFormat.getNumberInstance(Locale("id", "ID")).format(points)
                    binding.tvTukuPointValue.text = "$formattedPoints Poin"
                    val currentXp = document.getLong("currentXp")?.toInt() ?: 0
                    val tierName: String
                    val nextTargetXp: Int

                    when {
                        currentXp >= 1000 -> {
                            tierName = "Diamond"
                            nextTargetXp = 1000
                        }
                        currentXp >= 500 -> {
                            tierName = "Platinum"
                            nextTargetXp = 1000
                        }
                        currentXp >= 250 -> {
                            tierName = "Gold"
                            nextTargetXp = 500
                        }
                        currentXp >= 100 -> {
                            tierName = "Silver"
                            nextTargetXp = 250
                        }
                        else -> {
                            tierName = "Bronze"
                            nextTargetXp = 100
                        }
                    }
                    if (binding.tvLevelValue != null) {
                        binding.tvLevelValue.text = tierName

                        if (tierName == "Diamond") {
                            binding.tvExpValue.text = "MAX Level"
                            binding.progressBarExp.max = 100
                            binding.progressBarExp.progress = 100
                        } else {
                            binding.tvExpValue.text = "$currentXp / $nextTargetXp XP"
                            binding.progressBarExp.max = nextTargetXp
                            binding.progressBarExp.progress = currentXp
                        }
                    }
                }
            }
    }

    private fun loadMenuFromJson() {
        try {
            val inputStream = requireContext().assets.open("menu_items.json")
            val reader = InputStreamReader(inputStream)
            val menuDataType = object : TypeToken<MenuData>() {}.type
            val data: MenuData = Gson().fromJson(reader, menuDataType)
            allRealMenus = data.menu
        } catch (e: Exception) {
            Log.e("HomeFragment", "JSON Error: ${e.message}")
            allRealMenus = emptyList()
        }
    }

    private fun setupBudgetOptimizer() {
        binding.sliderBudget.addOnChangeListener { _, value, _ ->
            binding.tvBudgetDisplay.text = "Rp ${value.toInt()}"
        }

        binding.btnCariRekomendasi.setOnClickListener {
            val budget = binding.sliderBudget.value
            try {

                val classifier = BudgetClassifier(requireContext(), "budget_tuku_v2.tflite")
                val normalizedBudget = budget / 100000.0f
                val resultIndex = classifier.predict(normalizedBudget)


                val filteredRealData = when(resultIndex) {
                    0 -> allRealMenus.filter { getValidPrice(it) < 18000 }
                    1 -> allRealMenus.filter { getValidPrice(it) in 18000..32000 }
                    2 -> allRealMenus.filter { getValidPrice(it) > 32000 }
                    else -> emptyList()
                }


                val selectedMenu: MenuItem = if (filteredRealData.isNotEmpty()) {
                    filteredRealData.random()
                } else {
                    getDummyFallback(resultIndex)
                }


                binding.layoutResultML.visibility = View.VISIBLE
                binding.tvResultName.text = selectedMenu.name
                binding.tvResultPrice.text = "Rp ${getValidPrice(selectedMenu)}"

                Glide.with(this)
                    .load(selectedMenu.image)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivResultImage)


                binding.layoutResultML.setOnClickListener {
                    try {
                        val bundle = Bundle()
                        bundle.putParcelable("product", selectedMenu)
                        findNavController().navigate(R.id.action_nav_home_to_productDetailFragment, bundle)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal membuka detail", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(context, "Rekomendasi ditemukan!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat AI", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getValidPrice(menu: MenuItem): Int {
        return menu.price_iced ?: menu.price_hot ?: 0
    }

    private fun getDummyFallback(categoryIndex: Int): MenuItem {
        return when(categoryIndex) {
            0 -> createDummyMenu("Donat Kampoeng", "Donat gula halus.", 8000, "")
            1 -> createDummyMenu("Kopi Susu Tetangga", "Kopi susu gula aren.", 20000, "")
            2 -> createDummyMenu("TUKUCUR 1 Liter", "Stok kopi.", 85000, "")
            else -> createDummyMenu("Menu Spesial", "Rekomendasi.", 20000, "")
        }
    }

    private fun createDummyMenu(name: String, desc: String, price: Int, img: String): MenuItem {
        return MenuItem(
            id = (1000..9999).random(),
            category = "Recommendation",
            name = name,
            description = desc,
            image = img,
            createdAt = null,
            price_hot = price,
            price_iced = price
        )
    }

    override fun onResume() {
        super.onResume()
        loadSavedLocation()
        getVoucherCount()
        getUnreadNotificationCount()
    }

    private fun loadSavedLocation() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val savedBranchName = prefs.getString("selectedBranchName", "Pilih Cabang")
        binding.tvCurrentLocation.text = savedBranchName
    }

    private fun getVoucherCount() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        var totalCount = 0

        db.collection("vouchers").whereEqualTo("isActive", true).get()
            .addOnSuccessListener { globalDocs ->
                totalCount += globalDocs.size()

                db.collection("users").document(uid).collection("vouchers")
                    .get()
                    .addOnSuccessListener { privateDocs ->
                        totalCount += privateDocs.size()

                        updateVoucherCountUI(totalCount)
                    }
                    .addOnFailureListener {
                        updateVoucherCountUI(totalCount)
                    }
            }
    }

    private fun updateVoucherCountUI(count: Int) {
        if (_binding != null) binding.tvVoucherCount.text = "$count Voucher"
    }

    private fun getUnreadNotificationCount() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("userEmail", null) ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("notifications").whereEqualTo("userEmail", userEmail).whereEqualTo("isRead", false).get().addOnSuccessListener {
            updateNotificationBadge(it.size())
        }
    }

    private fun updateNotificationBadge(count: Int) {
        if (_binding != null) {
            binding.tvNotificationCount.text = if (count > 9) "9+" else count.toString()
            binding.tvNotificationCount.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.locationCard.setOnClickListener { pilihCabangLauncher.launch(Intent(requireActivity(), PilihCabangActivity::class.java)) }
        binding.btnNotification.setOnClickListener { findNavController().navigate(R.id.action_nav_home_to_notificationFragment) }
        binding.btnTukuPoint.setOnClickListener { findNavController().navigate(R.id.action_nav_home_to_tetanggaTukuFragment) }
        binding.ivTukuPoint.setOnClickListener { findNavController().navigate(R.id.action_nav_home_to_tukuPointFragment) }
        binding.referralCard.setOnClickListener { findNavController().navigate(R.id.action_nav_home_to_referralFragment) }
        binding.voucherCard.setOnClickListener { findNavController().navigate(R.id.action_nav_home_to_voucherSayaFragment) }
        binding.tukuCareCard.setOnClickListener { findNavController().navigate(R.id.action_nav_home_to_bantuanFragment) }
        binding.ivKodeRedeem.setOnClickListener { showRedeemDialog() }
        val changeTabToMenu = { if (activity is MainActivity) (activity as MainActivity).bottomNavigationView.selectedItemId = R.id.nav_cart }
        binding.takeAwayCard.setOnClickListener { changeTabToMenu() }
        binding.deliveryCard.setOnClickListener { changeTabToMenu() }
    }

    private fun showRedeemDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_redeem_code)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()

        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.80).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etCode = dialog.findViewById<EditText>(R.id.et_redeem_code)

        dialog.findViewById<Button>(R.id.btn_apply_redeem).setOnClickListener {
            val inputCode = etCode.text.toString().trim().uppercase()
            if (inputCode.isNotEmpty()) {
                processReferralRedeem(inputCode, dialog)
            } else {
                Toast.makeText(requireContext(), "Masukkan kode!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.findViewById<Button>(R.id.btn_cancel_redeem).setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun processReferralRedeem(inputCode: String, dialog: Dialog) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").whereEqualTo("ownReferralCode", inputCode).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "Kode tidak valid!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val referrerUid = documents.documents[0].id
                if (referrerUid == uid) {
                    Toast.makeText(requireContext(), "Tidak bisa pakai kode sendiri!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("users").document(uid).get().addOnSuccessListener { myDoc ->
                    if (myDoc.contains("referredBy")) {
                        Toast.makeText(requireContext(), "Anda sudah pernah klaim referral!", Toast.LENGTH_SHORT).show()
                    } else {
                        executeReward(uid, referrerUid, inputCode, dialog)
                    }
                }
            }
    }

    private fun executeReward(myUid: String, referrerUid: String, code: String, dialog: Dialog) {
        val db = FirebaseFirestore.getInstance()
        val voucherData = hashMapOf(
            "title" to "Voucher Diskon 50%",
            "desc" to "Hadiah referral dari kode $code",
            "status" to "active",
            "isActive" to true,
            "code" to "REF-$code",
            "discountAmount" to 50000.0,
            "minPurchase" to 0.0,
            "createdAt" to System.currentTimeMillis()
        )

        db.runTransaction { transaction ->
            val myRef = db.collection("users").document(myUid)
            val referrerRef = db.collection("users").document(referrerUid)

            transaction.update(myRef, "referredBy", code)

            transaction.set(myRef.collection("vouchers").document(), voucherData)
            transaction.set(referrerRef.collection("vouchers").document(), voucherData)
            null
        }.addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Berhasil! Voucher ditambahkan.", Toast.LENGTH_LONG).show()
            getVoucherCount()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBannerCarousel() {
        val bannerImages = listOf(R.drawable.banner_tuku_hut, R.drawable.tuku_banner, R.drawable.banner_tuku_mrt)
        binding.bannerViewPager.adapter = BannerAdapter(bannerImages)
        var currentPage = 0
        updateRunnable = Runnable {
            if (isAdded && _binding != null) {
                currentPage = (currentPage + 1) % bannerImages.size
                binding.bannerViewPager.setCurrentItem(currentPage, true)
            }
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() { updateRunnable?.let { handler.post(it) } }
        }, 3000, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        timer?.cancel()
        _binding = null
    }
}


class BudgetClassifier(context: Context, fileName: String) {
    private var interpreter: Interpreter? = null
    init {
        try {
            val assetManager = context.assets
            val descriptor = assetManager.openFd(fileName)
            val inputStream = FileInputStream(descriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = descriptor.startOffset
            val declaredLength = descriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) { e.printStackTrace() }
    }
    fun predict(budget: Float): Int {
        if (interpreter == null) return -1
        val input = FloatBuffer.allocate(1).apply { put(budget); rewind() }
        val output = Array(1) { FloatArray(3) }
        interpreter?.run(input, output)
        val probs = output[0]
        var maxIndex = 0
        for (i in 1 until probs.size) { if (probs[i] > probs[maxIndex]) maxIndex = i }
        return maxIndex
    }
}