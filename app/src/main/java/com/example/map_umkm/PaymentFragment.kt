package com.example.map_umkm

import android.location.Location
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CartItemAdapter
import com.example.map_umkm.model.Address
import com.example.map_umkm.model.Order
import com.example.map_umkm.viewmodel.CartViewModel
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
    private lateinit var layoutAddressSelected: LinearLayout
    private lateinit var tvDeliveryAddress: TextView
    private lateinit var btnChangeManual: Button
    private lateinit var btnSelectSaved: Button
    private lateinit var tvShippingCost: TextView
    private lateinit var layoutShippingCost: RelativeLayout
    private lateinit var cartAdapter: CartItemAdapter
    private lateinit var layoutPointsUsed: RelativeLayout
    private lateinit var tvPointsUsed: TextView

    private var shippingCost: Double = 0.0
    private var isDelivery = true
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private var branchLat: Double = 0.0
    private var branchLng: Double = 0.0
    private var userCurrentPoints: Long = 0
    private var isPointUsed = false

    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar_payment)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

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
        btnChangeManual = view.findViewById(R.id.btn_change_address_manual)
        btnSelectSaved = view.findViewById(R.id.btn_select_saved_address_list)
        tvShippingCost = view.findViewById(R.id.tvShippingCost)
        layoutShippingCost = view.findViewById(R.id.layout_shipping_cost)

        setupRecyclerView()
        loadBranchData()
        loadUserPoints()

        if (userLat == 0.0) {
            loadPrimaryAddress()
        }

        setupListeners()
        setupResultListeners()

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            updateTotals()
        }
    }

    private fun setupResultListeners() {
        setFragmentResultListener("request_alamat") { _, bundle ->
            val addressObj = bundle.getParcelable<Address>("data_alamat")
            if (addressObj != null) {
                val lat = addressObj.latitude ?: 0.0
                val lng = addressObj.longitude ?: 0.0
                updateUserLocation("${addressObj.label}\n${addressObj.fullAddress}", lat, lng)
                Toast.makeText(context, "Alamat digunakan: ${addressObj.label}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        btnChangeManual.setOnClickListener {
            val bundle = bundleOf("mode_create_new" to true)
            findNavController().navigate(R.id.action_paymentFragment_to_pilihLokasiFragment, bundle)
        }

        btnSelectSaved.setOnClickListener {
            findNavController().navigate(R.id.action_paymentFragment_to_alamatFragment)
        }

        rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            isDelivery = (checkedId == R.id.rb_delivery)
            layoutAddressSelected.visibility = if (isDelivery) View.VISIBLE else View.GONE
            layoutShippingCost.visibility = if (isDelivery) View.VISIBLE else View.GONE
            calculateShippingCost()
        }

        switchPoint.setOnCheckedChangeListener { _, isChecked ->
            isPointUsed = isChecked
            updateTotals()
        }

        btnPay.setOnClickListener {
            if (isDelivery && userLat == 0.0) {
                Toast.makeText(context, "Mohon pilih alamat pengiriman", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPaymentMethodDialog()
        }
    }

    private fun createOrder(paymentMethod: String, orderType: String) {
        if (currentUserUid == null) {
            Toast.makeText(context, "Error: User tidak terautentikasi.", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItems = cartViewModel.cartList.value ?: emptyList()
        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Keranjang Anda kosong.", Toast.LENGTH_SHORT).show()
            return
        }

        val subtotal = cartItems.sumOf { (if (it.selectedType == "iced") it.price_iced ?: 0 else it.price_hot)?.toDouble()?.times(it.quantity) ?: 0.0 }
        val tax = subtotal * 0.11
        val totalPayment = subtotal + tax + shippingCost

        val orderId = db.collection("orders").document().id
        val orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date())

        val newOrder = Order(
            orderId = orderId,
            userId = currentUserUid,
            items = cartItems,
            totalAmount = totalPayment, // Corrected parameter
            orderDate = orderDate,
            status = if (paymentMethod == "CASH") "Menunggu Pembayaran" else "Menunggu Konfirmasi",
            orderType = orderType,
            userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown User",
            userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        )

        db.collection("orders").document(orderId).set(newOrder)
            .addOnSuccessListener {
                if (paymentMethod == "QRIS") {
                    findNavController().navigate(R.id.action_paymentFragment_to_qrisFragment, bundleOf("orderId" to orderId, "totalPayment" to totalPayment))
                } else {
                    findNavController().navigate(R.id.action_paymentFragment_to_paymentSuccessFragment, bundleOf("paymentMethod" to "CASH"))
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal membuat pesanan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPaymentMethodDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment_choice, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val btnPayWithQris = dialogView.findViewById<Button>(R.id.btnPayWithQris)
        val btnPayWithCash = dialogView.findViewById<Button>(R.id.btnPayWithCash)
        val orderType = if (isDelivery) "Delivery" else "Take Away"

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

    private fun updateUserLocation(addressText: String, lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        tvDeliveryAddress.text = addressText
        calculateShippingCost()
    }

    private fun calculateShippingCost() {
        if (!isDelivery || userLat == 0.0 || branchLat == 0.0) {
            shippingCost = 0.0
        } else {
            val results = FloatArray(1)
            Location.distanceBetween(branchLat, branchLng, userLat, userLng, results)
            val distanceInKm = results[0] / 1000.0
            shippingCost = (distanceInKm * 3000.0).coerceIn(10000.0, 50000.0)
        }
        tvShippingCost.text = formatCurrency(shippingCost)
        updateTotals()
    }

    private fun updateTotals() {
        val cartItems = cartViewModel.cartList.value ?: emptyList()
        var subtotal = 0.0
        for (item in cartItems) {
            val price = if (item.selectedType == "iced") item.price_iced ?: 0 else item.price_hot ?: 0
            subtotal += (price.toDouble() * item.quantity)
        }

        val tax = subtotal * 0.11
        val totalWithTaxAndShip = subtotal + tax + shippingCost

        val discount = if (isPointUsed) {
            val pointsToUse = if (userCurrentPoints >= totalWithTaxAndShip) totalWithTaxAndShip else userCurrentPoints.toDouble()
            layoutPointsUsed.visibility = View.VISIBLE
            tvPointsUsed.text = "-${formatCurrency(pointsToUse)}"
            pointsToUse
        } else {
            layoutPointsUsed.visibility = View.GONE
            0.0
        }

        val finalTotal = (totalWithTaxAndShip - discount).coerceAtLeast(0.0)

        tvSubtotal.text = formatCurrency(subtotal)
        tvTax.text = formatCurrency(tax)
        tvTotalPayment.text = formatCurrency(finalTotal)
    }

    private fun loadPrimaryAddress() {
        if (currentUserUid == null) return
        db.collection("users").document(currentUserUid).collection("addresses")
            .whereEqualTo("primary", true)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val addr = it.documents[0].toObject(Address::class.java)
                    if (addr != null) {
                        updateUserLocation(
                            "${addr.label}\n${addr.fullAddress}",
                            addr.latitude ?: 0.0,
                            addr.longitude ?: 0.0
                        )
                    }
                } else {
                    tvDeliveryAddress.text = "Belum ada alamat utama. Silakan pilih."
                }
            }
    }

    private fun loadBranchData() {
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", android.content.Context.MODE_PRIVATE)
        branchLat = prefs.getString("selectedBranchLat", "-6.175392")?.toDouble() ?: -6.175392
        branchLng = prefs.getString("selectedBranchLng", "106.827153")?.toDouble() ?: 106.827153
    }

    private fun loadUserPoints() {
        if (currentUserUid == null) return
        db.collection("users").document(currentUserUid).get().addOnSuccessListener {
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
