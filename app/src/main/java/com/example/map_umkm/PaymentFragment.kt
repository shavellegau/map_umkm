package com.example.map_umkm

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CartItemAdapter
import com.example.map_umkm.model.Address
import com.example.map_umkm.model.Order
import com.example.map_umkm.model.Voucher
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var btnPay: Button
    private lateinit var switchPoint: SwitchMaterial
    private lateinit var tvUserPoints: TextView
    private lateinit var rgOrderType: RadioGroup
    private lateinit var layoutAddressSelected: MaterialCardView
    private lateinit var tvDeliveryAddress: TextView
    private lateinit var tvDeliveryAddressTitle: TextView
    private lateinit var btnChangeAddressManual: Button
    private lateinit var btnSelectSavedAddressList: Button
    private lateinit var tvShippingCost: TextView
    private lateinit var layoutShippingCost: RelativeLayout
    private lateinit var cartAdapter: CartItemAdapter
    private lateinit var layoutPointsUsed: RelativeLayout
    private lateinit var tvPointsUsed: TextView
    private lateinit var btnSelectVoucher: MaterialCardView
    private lateinit var tvVoucherSelection: TextView
    private lateinit var layoutVoucherDiscount: RelativeLayout
    private lateinit var tvVoucherDiscount: TextView
    private lateinit var tvPointsEarned: TextView
    private lateinit var tvExpEarned: TextView

    private var shippingCost: Double = 0.0
    private var isDelivery = true
    private var userCurrentPoints: Long = 0
    private var isPointUsed = false
    private var selectedAddress: Address? = null
    private var selectedVoucher: Voucher? = null

    private val db = FirebaseFirestore.getInstance()
    private var branchLat: Double = 0.0
    private var branchLng: Double = 0.0

    private val args: PaymentFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar_payment)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        initializeViews(view)

        isDelivery = args.orderType.equals("delivery", ignoreCase = true)
        if (isDelivery) {
            rgOrderType.check(R.id.rb_delivery)
        } else {
            rgOrderType.check(R.id.rb_take_away)
        }

        setupRecyclerView()
        loadBranchData()
        loadUserPoints()

        if (selectedAddress == null) {
            loadPrimaryAddress()
        }

        setupListeners()
        setupResultListeners()
        updateTotals()

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            updateTotals()
        }
    }

    private fun initializeViews(view: View) {
        rvOrderList = view.findViewById(R.id.rv_order_list)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)
        btnPay = view.findViewById(R.id.btnPay)
        switchPoint = view.findViewById(R.id.switchPoint)
        tvUserPoints = view.findViewById(R.id.tvUserPoints)
        rgOrderType = view.findViewById(R.id.rg_order_type)
        layoutPointsUsed = view.findViewById(R.id.layout_points_used)
        tvPointsUsed = view.findViewById(R.id.tvPointsUsed)
        layoutAddressSelected = view.findViewById(R.id.layout_address_selected)
        tvDeliveryAddress = view.findViewById(R.id.tv_delivery_address)
        tvDeliveryAddressTitle = view.findViewById(R.id.tv_delivery_address_title)
        btnChangeAddressManual = view.findViewById(R.id.btn_change_address_manual)
        btnSelectSavedAddressList = view.findViewById(R.id.btn_select_saved_address_list)
        tvShippingCost = view.findViewById(R.id.tvShippingCost)
        layoutShippingCost = view.findViewById(R.id.layout_shipping_cost)
        btnSelectVoucher = view.findViewById(R.id.btn_select_voucher)
        tvVoucherSelection = view.findViewById(R.id.tv_voucher_selection)
        layoutVoucherDiscount = view.findViewById(R.id.layout_voucher_discount)
        tvVoucherDiscount = view.findViewById(R.id.tv_voucher_discount)
        tvPointsEarned = view.findViewById(R.id.tv_points_earned)
        tvExpEarned = view.findViewById(R.id.tv_exp_earned)
    }

    private fun setupResultListeners() {
        setFragmentResultListener("request_alamat") { _, bundle ->
            val addressObj = bundle.getParcelable<Address>("data_alamat")
            if (addressObj != null) {
                selectedAddress = addressObj
                updateUserLocation(addressObj)
                Toast.makeText(context, "Alamat digunakan: ${addressObj.label}", Toast.LENGTH_SHORT).show()
            }
        }

        setFragmentResultListener("request_voucher") { _, bundle ->
            val voucher = bundle.getParcelable<Voucher>("selected_voucher")
            if (voucher != null) {
                selectedVoucher = voucher
                tvVoucherSelection.text = voucher.code
                updateTotals()
            }
        }
    }

    private fun setupListeners() {
        btnChangeAddressManual.setOnClickListener {
             val bundle = bundleOf("from_payment" to true)
            findNavController().navigate(R.id.action_paymentFragment_to_pilihLokasiFragment, bundle)
        }

        btnSelectSavedAddressList.setOnClickListener {
            findNavController().navigate(R.id.action_paymentFragment_to_alamatFragment)
        }

        rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            isDelivery = (checkedId == R.id.rb_delivery)
            layoutAddressSelected.visibility = if (isDelivery) View.VISIBLE else View.GONE
            layoutShippingCost.visibility = if (isDelivery) View.VISIBLE else View.GONE
            if(!isDelivery) {
                shippingCost = 0.0
            }
            calculateShippingCost()
        }

        switchPoint.setOnCheckedChangeListener { _, isChecked ->
            isPointUsed = isChecked
            updateTotals()
        }

        btnSelectVoucher.setOnClickListener {
            if (selectedVoucher == null) {
                val action = PaymentFragmentDirections.actionPaymentFragmentToVoucherSayaFragment()
                findNavController().navigate(action)
            } else {
                showVoucherOptionsDialog()
            }
        }

        btnPay.setOnClickListener {
            if (isDelivery && selectedAddress == null) {
                Toast.makeText(context, "Mohon pilih alamat pengiriman", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if ((cartViewModel.cartList.value ?: emptyList()).isEmpty()){
                Toast.makeText(context, "Keranjang Anda kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPaymentMethodDialog()
        }
    }

    private fun showVoucherOptionsDialog() {
        val options = arrayOf("Ganti Voucher", "Hapus Voucher")
        AlertDialog.Builder(requireContext())
            .setTitle("Opsi Voucher")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val action = PaymentFragmentDirections.actionPaymentFragmentToVoucherSayaFragment()
                        findNavController().navigate(action)
                    }
                    1 -> {
                        selectedVoucher = null
                        tvVoucherSelection.text = "Pilih Voucher / Diskon"
                        updateTotals()
                        Toast.makeText(context, "Voucher dihapus", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createOrder(paymentMethod: String, orderType: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Error: Sesi pengguna tidak valid. Silakan login kembali.", Toast.LENGTH_LONG).show()
            return
        }
        val currentUserId = currentUser.uid

        val cartItems = cartViewModel.cartList.value!!

        val subtotal = cartItems.sumOf { (if (it.selectedType == "iced") it.price_iced else it.price_hot)?.toDouble()?.times(it.quantity) ?: 0.0 }
        val pointsEarned = (subtotal * 0.10).toLong()
        val expEarned = (subtotal * 0.04).toLong()
        val tax = subtotal * 0.11
        val shipping = if(isDelivery) shippingCost else 0.0
        var totalWithTaxAndShip = subtotal + tax + shipping

        val voucherDiscount = if (selectedVoucher != null && subtotal >= selectedVoucher!!.minPurchase) {
            selectedVoucher!!.discountAmount
        } else {
            0.0
        }
        totalWithTaxAndShip -= voucherDiscount

        val pointsToUse = if (isPointUsed) (userCurrentPoints.toDouble()).coerceAtMost(totalWithTaxAndShip) else 0.0

        val finalTotal = (totalWithTaxAndShip - pointsToUse).coerceAtLeast(0.0)

        val orderId = db.collection("orders").document().id
        val orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

        val newOrder = Order(
            orderId = orderId,
            userId = currentUserId,
            items = cartItems,
            totalAmount = finalTotal,
            orderDate = orderDate,
            status = if (paymentMethod == "CASH") "Menunggu Pembayaran" else "Menunggu Konfirmasi",
            orderType = orderType,
            userName = currentUser.displayName ?: "Unknown User",
            userEmail = currentUser.email ?: "",
            deliveryAddress = if(isDelivery) selectedAddress else null,
            userToken = null,

            subtotal = subtotal,
            tax = tax,
            shippingCost = shipping,
            voucherDiscount = voucherDiscount,
            pointsUsed = pointsToUse,
            pointsEarned = pointsEarned,
            expEarned = expEarned
        )

        db.collection("orders").document(orderId).set(newOrder)
            .addOnSuccessListener {
                Log.d("PaymentFragment", "Order created successfully with ID: $orderId")
                val bundle = bundleOf("paymentMethod" to paymentMethod, "orderId" to orderId, "totalPayment" to finalTotal)
                if (paymentMethod == "QRIS") {
                    findNavController().navigate(R.id.action_paymentFragment_to_qrisFragment, bundle)
                } else {
                    findNavController().navigate(R.id.action_paymentFragment_to_paymentSuccessFragment, bundle)
                }
            }
            .addOnFailureListener { e ->
                Log.e("PaymentFragment", "Failed to create order", e)
                Toast.makeText(context, "Gagal membuat pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPaymentMethodDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment_choice, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val btnPayWithQris = dialogView.findViewById<Button>(R.id.btnPayWithQris)
        val btnPayWithCash = dialogView.findViewById<Button>(R.id.btnPayWithCash)
        val orderType = if (isDelivery) "Delivery" else "Take Away"

        if (isDelivery) {
            btnPayWithCash.visibility = View.GONE
        }

        btnPayWithQris.setOnClickListener {
            createOrder("QRIS", orderType)
            dialog.dismiss()
        }

        btnPayWithCash.setOnClickListener {
            createOrder("CASH", orderType)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateUserLocation(address: Address) {
        selectedAddress = address
        tvDeliveryAddress.text = "${address.label}\n${address.fullAddress}"
        tvDeliveryAddress.visibility = View.VISIBLE
        tvDeliveryAddressTitle.visibility = View.GONE
        calculateShippingCost()
    }

    private fun calculateShippingCost() {
        if (!isDelivery || selectedAddress == null || branchLat == 0.0) {
            shippingCost = 0.0
        } else {
            val results = FloatArray(1)
            Location.distanceBetween(branchLat, branchLng, selectedAddress!!.latitude!!, selectedAddress!!.longitude!!, results)
            val distanceInKm = results[0] / 1000.0
            shippingCost = (distanceInKm * 2500.0).coerceIn(8000.0, 30000.0)
        }
        tvShippingCost.text = formatCurrency(shippingCost)
        updateTotals()
    }

    private fun updateTotals() {
        val cartItems = cartViewModel.cartList.value ?: emptyList()
        val subtotal = cartItems.sumOf { (if (it.selectedType == "iced") it.price_iced else it.price_hot)?.toDouble()?.times(it.quantity) ?: 0.0 }
        val pointsEarned = (subtotal * 0.10).toLong()
        val expEarned = (subtotal * 0.04).toLong()

        tvPointsEarned.text = "$pointsEarned Poin"
        tvExpEarned.text = "$expEarned EXP"

        val tax = subtotal * 0.11
        val shipping = if(isDelivery) shippingCost else 0.0
        var totalWithTaxAndShip = subtotal + tax + shipping

        val voucherDiscount = if (selectedVoucher != null && subtotal >= selectedVoucher!!.minPurchase) {
            selectedVoucher!!.discountAmount
        } else {
            0.0
        }

        if (selectedVoucher != null && subtotal < selectedVoucher!!.minPurchase) {
            Toast.makeText(context, "Minimal pembelian untuk voucher ini adalah ${formatCurrency(selectedVoucher!!.minPurchase)}", Toast.LENGTH_SHORT).show()
            selectedVoucher = null
            tvVoucherSelection.text = "Pilih Voucher / Diskon"
        }

        layoutVoucherDiscount.visibility = if(voucherDiscount > 0) View.VISIBLE else View.GONE
        tvVoucherDiscount.text = "-${formatCurrency(voucherDiscount)}"
        totalWithTaxAndShip -= voucherDiscount

        val pointsToUse = if (isPointUsed) (userCurrentPoints.toDouble()).coerceAtMost(totalWithTaxAndShip) else 0.0

        layoutPointsUsed.visibility = if(isPointUsed && pointsToUse > 0) View.VISIBLE else View.GONE
        tvPointsUsed.text = "-${formatCurrency(pointsToUse)}"

        val finalTotal = (totalWithTaxAndShip - pointsToUse).coerceAtLeast(0.0)

        tvSubtotal.text = formatCurrency(subtotal)
        tvTax.text = formatCurrency(tax)
        tvTotalPayment.text = formatCurrency(finalTotal)
        btnPay.text = "Bayar ${formatCurrency(finalTotal)}"
    }

    private fun loadPrimaryAddress() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).collection("addresses")
            .whereEqualTo("primary", true)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val addr = it.documents[0].toObject(Address::class.java)
                    if (addr != null) {
                        updateUserLocation(addr)
                    }
                } else {
                    tvDeliveryAddressTitle.text = "Alamat pengiriman belum dipilih"
                    tvDeliveryAddress.visibility = View.GONE
                    tvDeliveryAddressTitle.visibility = View.VISIBLE
                }
            }.addOnFailureListener{
                 tvDeliveryAddressTitle.text = "Gagal memuat alamat. Silakan pilih."
                 tvDeliveryAddress.visibility = View.GONE
                 tvDeliveryAddressTitle.visibility = View.VISIBLE
            }
    }

    private fun loadBranchData() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", android.content.Context.MODE_PRIVATE)
        branchLat = prefs.getString("selectedBranchLat", "0.0")?.toDoubleOrNull() ?: 0.0
        branchLng = prefs.getString("selectedBranchLng", "0.0")?.toDoubleOrNull() ?: 0.0
    }

    private fun loadUserPoints() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener {
            userCurrentPoints = it.getLong("tukuPoints") ?: 0L
            tvUserPoints.text = "Saldo Poin: $userCurrentPoints"
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            items = mutableListOf(),
            onQuantityChanged = { updateTotals() },
            onDeleteItem = { product ->
                cartViewModel.deleteItem(product)
            }
        )
        rvOrderList.layoutManager = LinearLayoutManager(context)
        rvOrderList.adapter = cartAdapter
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount)
    }
}
