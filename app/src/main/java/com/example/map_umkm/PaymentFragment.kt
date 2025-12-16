package com.example.map_umkm

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CartItemAdapter
import com.example.map_umkm.model.Address
import com.example.map_umkm.model.Order
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class PaymentFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()

    // --- View Components ---
    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var btnPay: Button

    // --- Voucher Components ---
    private lateinit var layoutVoucherSelect: LinearLayout
    private lateinit var tvSelectedVoucherName: TextView
    private lateinit var btnRemoveVoucher: ImageView
    private lateinit var layoutDiscountInfo: RelativeLayout
    private lateinit var tvDiscountInfo: TextView

    // --- Point Components ---
    private lateinit var switchPoint: SwitchMaterial
    private lateinit var tvUserPoints: TextView

    // --- Delivery Components ---
    private lateinit var rgOrderType: RadioGroup
    private lateinit var rbDelivery: RadioButton
    private lateinit var rbTakeAway: RadioButton
    private lateinit var layoutDeliveryInfo: LinearLayout
    private lateinit var layoutAddressSelected: LinearLayout
    private lateinit var layoutAddressEmpty: LinearLayout
    private lateinit var tvDeliveryAddress: TextView
    private lateinit var btnChangeAddress: Button
    private lateinit var btnAddNewAddress: Button
    private lateinit var tvShippingCost: TextView
    private lateinit var layoutShippingCost: RelativeLayout

    private lateinit var cartAdapter: CartItemAdapter

    // --- Logic Variables ---
    private var voucherDiscountAmount: Double = 0.0
    private var pointDiscountAmount: Double = 0.0
    private var finalTotalAmount: Double = 0.0
    private var shippingCost: Double = 0.0
    private var pointsUsedForTransaction: Long = 0 // Menyimpan berapa poin yg akan dipotong

    // Status
    private var isDelivery = false
    private var isPointUsed = false

    // Data User
    private var userCurrentPoints: Long = 0

    // Koordinat
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private var branchLat: Double = 0.0
    private var branchLng: Double = 0.0
    private var selectedAddressString: String = ""

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val currentUserUid by lazy { FirebaseAuth.getInstance().uid }

    // Launcher Peta
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val address = data?.getStringExtra("selectedAddress")
            val lat = data?.getDoubleExtra("selectedLat", 0.0) ?: 0.0
            val lng = data?.getDoubleExtra("selectedLng", 0.0) ?: 0.0

            if (address != null) {
                updateUserLocationManual(address, lat, lng)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        initializeViews(view)
        setupRecyclerView()

        // 1. Load Data
        loadBranchData()
        loadUserPoints()

        // 2. Setup Listener
        setupListeners()

        // 3. Observer Voucher
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("selectedVoucherCode")
            ?.observe(viewLifecycleOwner) { code ->
                if (!code.isNullOrEmpty()) {
                    checkVoucherToFirebase(code)
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("selectedVoucherCode")
                }
            }

        // 4. Observer Keranjang
        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            updateTotals()
        }

        // Default: Take Away
        rbTakeAway.isChecked = true
        handleOrderTypeChange(false)

        return view
    }

    private fun loadBranchData() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val latStr = prefs.getString("selectedBranchLat", "0.0")
        val lngStr = prefs.getString("selectedBranchLng", "0.0")

        branchLat = latStr?.toDoubleOrNull() ?: 0.0
        branchLng = lngStr?.toDoubleOrNull() ?: 0.0
    }

    private fun loadUserPoints() {
        if (currentUserUid == null) return
        db.collection("users").document(currentUserUid!!).get()
            .addOnSuccessListener { snapshot ->
                // Pastikan nama field sesuai database ("tukuPoints" atau "points")
                userCurrentPoints = snapshot.getLong("tukuPoints") ?: 0L
                tvUserPoints.text = "Saldo Poin: ${NumberFormat.getInstance(Locale("in", "ID")).format(userCurrentPoints)}"

                // Jika poin 0, matikan switch otomatis
                if (userCurrentPoints <= 0) {
                    switchPoint.isEnabled = false
                    switchPoint.isChecked = false
                }
            }
    }

    private fun initializeViews(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_payment)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        rvOrderList = view.findViewById(R.id.rv_order_list)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)
        btnPay = view.findViewById(R.id.btnPay)

        layoutVoucherSelect = view.findViewById(R.id.layout_voucher_select)
        tvSelectedVoucherName = view.findViewById(R.id.tv_selected_voucher_name)
        btnRemoveVoucher = view.findViewById(R.id.btn_remove_voucher)
        layoutDiscountInfo = view.findViewById(R.id.layout_discount_info)
        tvDiscountInfo = view.findViewById(R.id.tvDiscountInfo)

        switchPoint = view.findViewById(R.id.switchPoint)
        tvUserPoints = view.findViewById(R.id.tvUserPoints)

        rgOrderType = view.findViewById(R.id.rg_order_type)
        rbDelivery = view.findViewById(R.id.rb_delivery)
        rbTakeAway = view.findViewById(R.id.rb_take_away)

        layoutDeliveryInfo = view.findViewById(R.id.layout_delivery_info)
        layoutAddressSelected = view.findViewById(R.id.layout_address_selected)
        layoutAddressEmpty = view.findViewById(R.id.layout_address_empty)
        tvDeliveryAddress = view.findViewById(R.id.tv_delivery_address)

        btnChangeAddress = view.findViewById(R.id.btn_change_address)
        btnAddNewAddress = view.findViewById(R.id.btn_add_new_address)

        tvShippingCost = view.findViewById(R.id.tvShippingCost)
        layoutShippingCost = view.findViewById(R.id.layout_shipping_cost)
    }

    private fun setupListeners() {
        btnPay.setOnClickListener {
            if (cartViewModel.cartList.value.isNullOrEmpty()) {
                Toast.makeText(context, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isDelivery && userLat == 0.0) {
                Toast.makeText(context, "Silakan pilih alamat pengiriman.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showPaymentChoiceDialog()
        }

        layoutVoucherSelect.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_paymentFragment_to_voucherSayaFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Navigasi Voucher Error", Toast.LENGTH_SHORT).show()
            }
        }
        btnRemoveVoucher.setOnClickListener { resetVoucher() }

        rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            handleOrderTypeChange(checkedId == R.id.rb_delivery)
        }

        val openMapAction = View.OnClickListener {
            val intent = Intent(requireContext(), PetaPilihLokasiActivity::class.java)
            mapLauncher.launch(intent)
        }
        btnChangeAddress.setOnClickListener(openMapAction)
        btnAddNewAddress.setOnClickListener(openMapAction)

        // --- LOGIC TOGGLE POIN UPDATE ---
        switchPoint.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (userCurrentPoints > 0) {
                    isPointUsed = true
                    updateTotals() // Hitung ulang diskon di updateTotals
                } else {
                    switchPoint.isChecked = false
                    isPointUsed = false
                    Toast.makeText(context, "Anda tidak memiliki poin", Toast.LENGTH_SHORT).show()
                }
            } else {
                isPointUsed = false
                pointDiscountAmount = 0.0
                pointsUsedForTransaction = 0
                updateTotals()
            }
        }
    }

    private fun updateUserLocationManual(address: String, lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        selectedAddressString = address
        tvDeliveryAddress.text = address
        layoutAddressEmpty.visibility = View.GONE
        layoutAddressSelected.visibility = View.VISIBLE
        calculateShippingCost()
        updateTotals()
    }

    private fun handleOrderTypeChange(isDeliveryMode: Boolean) {
        isDelivery = isDeliveryMode
        if (isDelivery) {
            layoutDeliveryInfo.visibility = View.VISIBLE
            layoutShippingCost.visibility = View.VISIBLE
            if (userLat != 0.0) calculateShippingCost() else shippingCost = 0.0
        } else {
            layoutDeliveryInfo.visibility = View.GONE
            layoutShippingCost.visibility = View.GONE
            shippingCost = 0.0
        }
        updateTotals()
    }

    private fun calculateShippingCost() {
        if (!isDelivery || userLat == 0.0 || branchLat == 0.0) {
            shippingCost = 0.0
            updateTotals()
            return
        }

        val results = FloatArray(1)
        Location.distanceBetween(branchLat, branchLng, userLat, userLng, results)
        val distanceInMeters = results[0].toDouble()
        val distanceInKm = distanceInMeters / 1000.0

        var cost = distanceInKm * 3000.0
        if (cost < 10000) cost = 10000.0
        if (cost > 50000) cost = 50000.0

        shippingCost = cost
        val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvShippingCost.text = "${formatRp.format(shippingCost)} (${String.format("%.1f", distanceInKm)} km)"
        updateTotals()
    }

    private fun updateTotals() {
        val subtotal = calculateSubtotal()
        val tax = subtotal * 0.11
        val currentShipping = if (isDelivery) shippingCost else 0.0

        // 1. Hitung total sebelum dikurangi poin (tapi sudah dikurangi voucher)
        val totalBeforePoint = (subtotal + tax + currentShipping) - voucherDiscountAmount

        // 2. Logika Poin Dinamis (1 Poin = Rp 1)
        if (isPointUsed && totalBeforePoint > 0) {
            // Jika poin user lebih banyak dari tagihan -> Diskon = Tagihan (Bayar 0)
            // Jika poin user lebih sedikit -> Diskon = Semua poin user
            if (userCurrentPoints >= totalBeforePoint) {
                pointDiscountAmount = totalBeforePoint
                pointsUsedForTransaction = totalBeforePoint.toLong()
            } else {
                pointDiscountAmount = userCurrentPoints.toDouble()
                pointsUsedForTransaction = userCurrentPoints
            }
        } else {
            pointDiscountAmount = 0.0
            pointsUsedForTransaction = 0
        }

        // 3. Hitung Total Akhir
        var total = totalBeforePoint - pointDiscountAmount
        if (total < 0) total = 0.0
        finalTotalAmount = total

        // Update UI Text
        tvSubtotal.text = formatCurrency(subtotal)
        tvTax.text = formatCurrency(tax)
        tvTotalPayment.text = formatCurrency(total)

        // Update Text Button
        if (total == 0.0) {
            btnPay.text = "Bayar Sekarang (Gratis)"
        } else {
            btnPay.text = "Bayar ${formatCurrency(total)}"
        }

        // Update Info Diskon (Gabungan Voucher + Poin)
        val totalDisc = voucherDiscountAmount + pointDiscountAmount
        if (totalDisc > 0) {
            layoutDiscountInfo.visibility = View.VISIBLE

            // Opsional: Detailkan info diskon
            val discTextBuilder = StringBuilder()
            if (voucherDiscountAmount > 0) discTextBuilder.append("Voucher: -${formatCurrency(voucherDiscountAmount)} ")
            if (pointDiscountAmount > 0) discTextBuilder.append("Poin: -${formatCurrency(pointDiscountAmount)}")

            tvDiscountInfo.text = discTextBuilder.toString()
        } else {
            layoutDiscountInfo.visibility = View.GONE
        }
    }

    private fun calculateSubtotal(): Double {
        return cartViewModel.cartList.value?.sumOf {
            ((if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0) * it.quantity.toDouble()
        } ?: 0.0
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount)
    }

    // --- LOGIKA VOUCHER ---
    private fun resetVoucher() {
        voucherDiscountAmount = 0.0
        tvSelectedVoucherName.text = "Pilih Voucher / Diskon"
        tvSelectedVoucherName.setTextColor(Color.BLACK)
        btnRemoveVoucher.visibility = View.GONE
        updateTotals()
    }

    private fun applyVoucherLogic(discount: Double, minPurchase: Double, title: String) {
        if (calculateSubtotal() >= minPurchase) {
            voucherDiscountAmount = discount
            tvSelectedVoucherName.text = "$title (Hemat ${formatCurrency(voucherDiscountAmount)})"
            tvSelectedVoucherName.setTextColor(Color.parseColor("#4CAF50"))
            btnRemoveVoucher.visibility = View.VISIBLE
            Toast.makeText(context, "Voucher Berhasil Dipasang!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Min. belanja ${formatCurrency(minPurchase)}", Toast.LENGTH_SHORT).show()
            resetVoucher()
        }
        updateTotals()
    }

    private fun checkVoucherToFirebase(code: String) {
        db.collection("vouchers").document(code).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getBoolean("isActive") == true) {
                    val min = document.getDouble("minPurchase") ?: 0.0
                    val disc = document.getDouble("discountAmount") ?: 0.0
                    val title = document.getString("title") ?: code
                    applyVoucherLogic(disc, min, title)
                } else {
                    Toast.makeText(context, "Kode Voucher Tidak Valid/Aktif", Toast.LENGTH_SHORT).show()
                    resetVoucher()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal cek voucher", Toast.LENGTH_SHORT).show()
            }
    }

    // --- DIALOG PILIH PEMBAYARAN ---
    private fun showPaymentChoiceDialog() {
        // Jika total 0 (Gratis karena poin), langsung proses tanpa dialog pilih Cash/QRIS
        if (finalTotalAmount == 0.0) {
            processPaymentSequence(isCashPayment = true) // Anggap Cash tapi 0
            return
        }

        Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_payment_choice)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<Button>(R.id.btnPayWithQris).setOnClickListener {
                dismiss()
                processPaymentSequence(isCashPayment = false)
            }
            findViewById<Button>(R.id.btnPayWithCash).setOnClickListener {
                dismiss()
                processPaymentSequence(isCashPayment = true)
            }
            show()
        }
    }

    // --- PROSES PEMBAYARAN & POTONG POIN ---
    private fun processPaymentSequence(isCashPayment: Boolean) {
        setLoading(true)

        // 1. Cek Apakah Menggunakan Poin?
        if (isPointUsed && pointsUsedForTransaction > 0) {
            deductPointsAndCreateOrder(isCashPayment)
        } else {
            // Tidak pakai poin, langsung bikin order
            createOrder(isCashPayment)
        }
    }

    private fun deductPointsAndCreateOrder(isCashPayment: Boolean) {
        if (currentUserUid == null) return
        val userRef = db.collection("users").document(currentUserUid!!)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("tukuPoints") ?: 0L

            if (currentPoints >= pointsUsedForTransaction) {
                val newPoints = currentPoints - pointsUsedForTransaction
                transaction.update(userRef, "tukuPoints", newPoints)
            } else {
                throw Exception("Poin tidak cukup saat transaksi diproses!")
            }
        }.addOnSuccessListener {
            // Poin berhasil dipotong, lanjut simpan order
            createOrder(isCashPayment)
        }.addOnFailureListener { e ->
            setLoading(false)
            Toast.makeText(context, "Gagal memproses poin: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createOrder(isCashPayment: Boolean) {
        val currentCart = cartViewModel.cartList.value ?: return
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val tokenPrefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)

        val orderType = if (isDelivery) "Delivery" else "Take Away"

        // Address Object
        val addressData = if (isDelivery) {
            Address(
                id = "TEMP",
                recipientName = prefs.getString("userName", "User") ?: "",
                phoneNumber = "",
                fullAddress = selectedAddressString,
                latitude = userLat,
                longitude = userLng
            )
        } else null

        // Tentukan status: Jika gratis (Rp 0), langsung "Diproses" atau "Menunggu Konfirmasi"
        val initialStatus = if (finalTotalAmount == 0.0) "Menunggu Konfirmasi"
        else if (isCashPayment) "Menunggu Pembayaran"
        else "Menunggu Konfirmasi"

        val newOrder = Order(
            orderId = "TUKU-${System.currentTimeMillis()}",
            userEmail = prefs.getString("userEmail", "unknown@email.com") ?: "unknown",
            userName = prefs.getString("userName", "User") ?: "User",
            items = currentCart.toList(),
            totalAmount = finalTotalAmount,
            orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date()),
            status = initialStatus,
            userToken = tokenPrefs.getString("fcm_token", "") ?: "",
            deliveryAddress = addressData,
            orderType = orderType
        )

        db.collection("orders").document(newOrder.orderId).set(newOrder)
            .addOnSuccessListener {
                setLoading(false)
                cartViewModel.clearCart()

                // Navigasi
                val action = if (isCashPayment || finalTotalAmount == 0.0) {
                    PaymentFragmentDirections.actionPaymentFragmentToPaymentSuccessFragment(paymentMethod = "CASH")
                } else {
                    PaymentFragmentDirections.actionPaymentFragmentToQrisFragment()
                }
                findNavController().navigate(action)
                Toast.makeText(context, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(context, "Gagal menyimpan pesanan: ${e.message}", Toast.LENGTH_LONG).show()
                // NOTE: Jika poin sudah terpotong tapi createOrder gagal,
                // idealnya perlu logic rollback poin. Untuk simplicity, dibiarkan begini dulu.
            }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            mutableListOf(),
            onQuantityChanged = { resetVoucher() },
            onDeleteItem = { product ->
                cartViewModel.deleteItem(product)
                resetVoucher()
            }
        )
        rvOrderList.layoutManager = LinearLayoutManager(requireContext())
        rvOrderList.adapter = cartAdapter
    }

    private fun setLoading(isLoading: Boolean) {
        if (isAdded) {
            btnPay.isEnabled = !isLoading
            btnPay.text = if (isLoading) "Memproses..." else "Bayar ${formatCurrency(finalTotalAmount)}"
            switchPoint.isEnabled = !isLoading
            layoutVoucherSelect.isEnabled = !isLoading
        }
    }
}