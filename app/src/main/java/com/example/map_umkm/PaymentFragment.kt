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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()

    // View Components
    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var btnPay: Button

    // Voucher Components
    private lateinit var layoutVoucherSelect: LinearLayout
    private lateinit var tvSelectedVoucherName: TextView
    private lateinit var btnRemoveVoucher: ImageView
    private lateinit var layoutDiscountInfo: RelativeLayout
    private lateinit var tvDiscountInfo: TextView

    // Delivery Components
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

    // Logic Variables
    private var discountAmount: Double = 0.0
    private var finalTotalAmount: Double = 0.0
    private var shippingCost: Double = 0.0
    private var isDelivery = false

    // Koordinat
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private var branchLat: Double = 0.0
    private var branchLng: Double = 0.0

    // String Alamat untuk disimpan
    private var selectedAddressString: String = ""

    private val db by lazy { FirebaseFirestore.getInstance() }

    // LAUNCHER UNTUK BUKA PETA DAN TERIMA HASILNYA
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val address = data?.getStringExtra("selectedAddress")
            val lat = data?.getDoubleExtra("selectedLat", 0.0) ?: 0.0
            val lng = data?.getDoubleExtra("selectedLng", 0.0) ?: 0.0

            if (address != null) {
                // Update UI dengan data dari Peta
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

        // Load data cabang yang dipilih user sebelumnya
        loadBranchData()

        setupListeners()

        // Observer Voucher
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("selectedVoucherCode")
            ?.observe(viewLifecycleOwner) { code ->
                if (!code.isNullOrEmpty()) {
                    checkVoucherToFirebase(code)
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("selectedVoucherCode")
                }
            }

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            updateTotals()
        }

        // Default: Take Away dulu
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
            if (userLat != 0.0) {
                calculateShippingCost()
            } else {
                shippingCost = 0.0
            }
        } else {
            layoutDeliveryInfo.visibility = View.GONE
            layoutShippingCost.visibility = View.GONE
            shippingCost = 0.0
        }
        updateTotals()
    }

    // [FIXED] Perbaikan Type Mismatch Float vs Double
    private fun calculateShippingCost() {
        if (!isDelivery || userLat == 0.0 || branchLat == 0.0) {
            shippingCost = 0.0
            updateTotals()
            return
        }

        val results = FloatArray(1)
        Location.distanceBetween(branchLat, branchLng, userLat, userLng, results)

        // Convert Float results[0] to Double explicitly
        val distanceInMeters = results[0].toDouble()
        val distanceInKm = distanceInMeters / 1000.0

        // Rumus Harga: Rp 3.000 per KM
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

        var total = (subtotal + tax + currentShipping) - discountAmount
        if (total < 0) total = 0.0
        finalTotalAmount = total

        tvSubtotal.text = formatCurrency(subtotal)
        tvTax.text = formatCurrency(tax)
        tvTotalPayment.text = formatCurrency(total)
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
        discountAmount = 0.0
        tvSelectedVoucherName.text = "Pilih Voucher / Diskon"
        tvSelectedVoucherName.setTextColor(Color.BLACK)
        btnRemoveVoucher.visibility = View.GONE
        layoutDiscountInfo.visibility = View.GONE
        updateTotals()
    }

    private fun applyVoucherLogic(discount: Double, minPurchase: Double, title: String) {
        if (calculateSubtotal() >= minPurchase) {
            discountAmount = discount
            tvSelectedVoucherName.text = "$title (Hemat ${formatCurrency(discountAmount)})"
            tvSelectedVoucherName.setTextColor(Color.parseColor("#4CAF50"))
            btnRemoveVoucher.visibility = View.VISIBLE
            layoutDiscountInfo.visibility = View.VISIBLE
            tvDiscountInfo.text = "-${formatCurrency(discountAmount)}"
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

    private fun createOrder(isCashPayment: Boolean) {
        val currentCart = cartViewModel.cartList.value ?: return
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val tokenPrefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)

        val orderType = if (isDelivery) "Delivery" else "Take Away"

        // [FIXED] Menggunakan constructor Address yang benar (latitude & longitude, bukan latLng)
        val addressData = if (isDelivery) {
            Address(
                id = "TEMP",
                recipientName = prefs.getString("userName", "User") ?: "",
                phoneNumber = "",
                fullAddress = selectedAddressString,
                latitude = userLat,    // Benar
                longitude = userLng    // Benar
            )
        } else null

        val newOrder = Order(
            orderId = "TUKU-${System.currentTimeMillis()}",
            userEmail = prefs.getString("userEmail", "unknown@email.com") ?: "unknown",
            userName = prefs.getString("userName", "User") ?: "User",
            items = currentCart.toList(),
            totalAmount = finalTotalAmount,
            orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date()),
            status = if (isCashPayment) "Menunggu Pembayaran" else "Menunggu Konfirmasi",
            userToken = tokenPrefs.getString("fcm_token", "") ?: "",
            deliveryAddress = addressData,
            orderType = orderType
        )

        setLoading(true)
        db.collection("orders").document(newOrder.orderId).set(newOrder)
            .addOnSuccessListener {
                setLoading(false)
                cartViewModel.clearCart()

                val action = if (isCashPayment) {
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
            btnPay.text = if (isLoading) "Memproses..." else "Pilih Metode Pembayaran"
        }
    }

    private fun showPaymentChoiceDialog() {
        Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_payment_choice)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<Button>(R.id.btnPayWithQris).setOnClickListener {
                dismiss()
                createOrder(isCashPayment = false)
            }
            findViewById<Button>(R.id.btnPayWithCash).setOnClickListener {
                dismiss()
                createOrder(isCashPayment = true)
            }
            show()
        }
    }
}