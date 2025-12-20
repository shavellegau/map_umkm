package com.example.map_umkm

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CartItemAdapter
import com.example.map_umkm.model.Address
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class PaymentFragment : Fragment() {

    // ... (Variabel deklarasi tetap sama seperti kode Anda) ...
    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var btnPay: Button
    private lateinit var layoutVoucherSelect: LinearLayout
    private lateinit var tvSelectedVoucherName: TextView
    private lateinit var btnRemoveVoucher: ImageView
    private lateinit var layoutDiscountInfo: RelativeLayout
    private lateinit var tvDiscountInfo: TextView
    private lateinit var switchPoint: SwitchMaterial
    private lateinit var tvUserPoints: TextView
    private lateinit var rgOrderType: RadioGroup
    private lateinit var rbDelivery: RadioButton
    private lateinit var rbTakeAway: RadioButton
    private lateinit var layoutAddressSelected: LinearLayout
    private lateinit var tvDeliveryAddress: TextView
    private lateinit var btnChangeManual: Button
    private lateinit var btnSelectSaved: Button
    private lateinit var tvShippingCost: TextView
    private lateinit var layoutShippingCost: RelativeLayout
    private lateinit var cartAdapter: CartItemAdapter

    private var voucherDiscountAmount: Double = 0.0
    private var pointDiscountAmount: Double = 0.0
    private var finalTotalAmount: Double = 0.0
    private var shippingCost: Double = 0.0
    private var pointsUsedForTransaction: Long = 0
    private var isDelivery = true
    private var isPointUsed = false
    private var userCurrentPoints: Long = 0
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private var branchLat: Double = 0.0
    private var branchLng: Double = 0.0

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val currentUserUid by lazy { FirebaseAuth.getInstance().uid }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        initializeViews(view)
        setupRecyclerView()
        loadBranchData()
        loadUserPoints()
        setupListeners()
        setupResultListeners() // PERUBAHAN NAMA FUNGSI
        loadPrimaryAddress()

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            updateTotals()
        }

        return view
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
        layoutAddressSelected = view.findViewById(R.id.layout_address_selected)
        tvDeliveryAddress = view.findViewById(R.id.tv_delivery_address)
        btnChangeManual = view.findViewById(R.id.btn_change_address_manual)
        btnSelectSaved = view.findViewById(R.id.btn_select_saved_address_list)
        tvShippingCost = view.findViewById(R.id.tvShippingCost)
        layoutShippingCost = view.findViewById(R.id.layout_shipping_cost)
    }

    private fun setupListeners() {
        btnPay.setOnClickListener {
            if (isDelivery && userLat == 0.0) {
                Toast.makeText(context, "Pilih alamat pengiriman", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "Memproses Pembayaran...", Toast.LENGTH_SHORT).show()
        }

        rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            handleOrderTypeChange(checkedId == R.id.rb_delivery)
        }

        btnChangeManual.setOnClickListener {
            // Mengirim flag bahwa kita membuka peta dari halaman Payment
            val bundle = bundleOf("from_payment" to true)
            findNavController().navigate(R.id.action_paymentFragment_to_pilihLokasiFragment, bundle)
        }

        btnSelectSaved.setOnClickListener {
            findNavController().navigate(R.id.action_paymentFragment_to_alamatFragment)
        }

        switchPoint.setOnCheckedChangeListener { _, isChecked ->
            isPointUsed = isChecked
            updateTotals()
        }
    }

    // --- BAGIAN INI DIPERBAIKI ---
    private fun setupResultListeners() {
        // 1. Mendengarkan hasil dari Daftar Alamat (Saved Address)
        setFragmentResultListener("request_alamat") { _, bundle ->
            val addressObj = bundle.getParcelable<Address>("data_alamat")
            addressObj?.let {
                // Gunakan Elvis Operator (?:) karena latitude di Address sekarang Nullable
                val lat = it.latitude ?: 0.0
                val lng = it.longitude ?: 0.0
                updateUserLocation("${it.label}\n${it.fullAddress}", lat, lng)
            }
        }

        // 2. Mendengarkan hasil dari Peta Manual (Map Selection)
        setFragmentResultListener("request_manual_map") { _, bundle ->
            val resultAddress = bundle.getString("hasil_alamat") ?: ""
            val resultLat = bundle.getDouble("hasil_lat")
            val resultLng = bundle.getDouble("hasil_lng")

            updateUserLocation(resultAddress, resultLat, resultLng)
        }
    }

    private fun handleOrderTypeChange(isDeliveryMode: Boolean) {
        isDelivery = isDeliveryMode
        layoutAddressSelected.visibility = if (isDeliveryMode) View.VISIBLE else View.GONE
        layoutShippingCost.visibility = if (isDeliveryMode) View.VISIBLE else View.GONE
        calculateShippingCost()
    }

    private fun updateUserLocation(address: String, lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        tvDeliveryAddress.text = address
        calculateShippingCost()
    }

    private fun calculateShippingCost() {
        if (!isDelivery || userLat == 0.0) {
            shippingCost = 0.0
        } else {
            val results = FloatArray(1)
            Location.distanceBetween(branchLat, branchLng, userLat, userLng, results)
            val distanceInKm = results[0] / 1000.0
            shippingCost = (distanceInKm * 3000.0).coerceIn(10000.0, 50000.0)
            tvShippingCost.text = formatCurrency(shippingCost)
        }
        updateTotals()
    }

    private fun updateTotals() {
        val cartItems = cartViewModel.cartList.value ?: emptyList()

        val subtotal = cartItems.sumOf { item ->
            val unitPrice = if (item.selectedType == "iced") {
                item.price_iced ?: 0
            } else {
                item.price_hot ?: 0
            }
            unitPrice.toDouble() * item.quantity.toDouble()
        }

        val tax = subtotal * 0.11
        val totalBeforePoint = subtotal + tax + shippingCost - voucherDiscountAmount

        if (isPointUsed) {
            pointDiscountAmount = if (userCurrentPoints.toDouble() >= totalBeforePoint) totalBeforePoint else userCurrentPoints.toDouble()
            pointsUsedForTransaction = pointDiscountAmount.toLong()
        } else {
            pointDiscountAmount = 0.0
        }

        finalTotalAmount = (totalBeforePoint - pointDiscountAmount).coerceAtLeast(0.0)

        tvSubtotal.text = formatCurrency(subtotal)
        tvTax.text = formatCurrency(tax)
        tvTotalPayment.text = formatCurrency(finalTotalAmount)
    }

    private fun loadUserPoints() {
        currentUserUid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener {
                userCurrentPoints = it.getLong("tukuPoints") ?: 0L
                tvUserPoints.text = "Saldo Poin: $userCurrentPoints"
            }
        }
    }

    private fun loadBranchData() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        branchLat = prefs.getString("selectedBranchLat", "0.0")?.toDouble() ?: 0.0
        branchLng = prefs.getString("selectedBranchLng", "0.0")?.toDouble() ?: 0.0
    }

    private fun loadPrimaryAddress() {
        currentUserUid?.let { uid ->
            db.collection("users").document(uid).collection("addresses")
                .whereEqualTo("isPrimary", true).limit(1).get().addOnSuccessListener {
                    if (!it.isEmpty) {
                        val addr = it.documents[0].toObject(Address::class.java)
                        addr?.let { a ->
                            // Safety check untuk nullable latitude
                            updateUserLocation(a.fullAddress, a.latitude ?: 0.0, a.longitude ?: 0.0)
                        }
                    }
                }
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(mutableListOf(), { updateTotals() }, { cartViewModel.deleteItem(it) })
        rvOrderList.layoutManager = LinearLayoutManager(context)
        rvOrderList.adapter = cartAdapter
    }

    private fun formatCurrency(amount: Double) = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount)
}