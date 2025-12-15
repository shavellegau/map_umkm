package com.example.map_umkm

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
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

    // --- VIEW VOUCHER BARU (PENGGANTI EDIT TEXT) ---
    private lateinit var layoutVoucherSelect: LinearLayout
    private lateinit var tvSelectedVoucherName: TextView
    private lateinit var btnRemoveVoucher: ImageView
    // ------------------------------------------------

    private lateinit var layoutDiscountInfo: RelativeLayout
    private lateinit var tvDiscountInfo: TextView
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

    private var discountAmount: Double = 0.0
    private var finalTotalAmount: Double = 0.0
    private var shippingCost: Double = 0.0 // Ongkir (default 0)
    private var selectedAddress: Address? = null
    private var isDelivery = false

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Lokasi Toko (Contoh: Jakarta Pusat)
    private val storeLocation = LatLng(-6.2088, 106.8456)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        initializeViews(view)
        setupRecyclerView()
        setupListeners()
        setupAddressHandling()

        // --- OBSERVER UNTUK MENANGKAP VOUCHER DARI VOUCHERSAYAFRAGMENT ---
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("selectedVoucherCode")
            ?.observe(viewLifecycleOwner) { code ->
                if (!code.isNullOrEmpty()) {
                    // Cek validitas voucher ke Firebase saat data diterima
                    checkVoucherToFirebase(code)

                    // Hapus data dari savedStateHandle agar tidak tereksekusi ulang saat rotasi layar
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("selectedVoucherCode")
                }
            }

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            updateTotals()
        }

        // Default Take Away
        rbTakeAway.isChecked = true
        handleOrderTypeChange(R.id.rb_take_away)

        return view
    }

    override fun onResume() {
        super.onResume()
        if (isDelivery && selectedAddress == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                loadInitialAddress()
            }
        } else if (isDelivery) {
            updateAddressView()
            calculateShippingCost()
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

        // --- INISIALISASI KOMPONEN VOUCHER BARU ---
        layoutVoucherSelect = view.findViewById(R.id.layout_voucher_select)
        tvSelectedVoucherName = view.findViewById(R.id.tv_selected_voucher_name)
        btnRemoveVoucher = view.findViewById(R.id.btn_remove_voucher)
        // -------------------------------------------

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
            if (isDelivery && selectedAddress == null) {
                Toast.makeText(context, "Silakan pilih alamat pengiriman.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showPaymentChoiceDialog()
        }

        // --- LISTENER PILIH VOUCHER (NAVIGASI) ---
        layoutVoucherSelect.setOnClickListener {
            // Pastikan Anda sudah menambahkan <action> ini di nav_graph.xml pada fragment payment
            try {
                findNavController().navigate(R.id.action_paymentFragment_to_voucherSayaFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Navigasi belum diatur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // --- LISTENER HAPUS VOUCHER ---
        btnRemoveVoucher.setOnClickListener {
            resetVoucher()
        }

        rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            handleOrderTypeChange(checkedId)
        }

        val navigateToAddressList = {
            findNavController().navigate(R.id.action_paymentFragment_to_alamatFragment)
        }
        btnChangeAddress.setOnClickListener { navigateToAddressList() }
        btnAddNewAddress.setOnClickListener { navigateToAddressList() }
    }

    private fun setupAddressHandling() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Address>("selectedAddress")
            ?.observe(viewLifecycleOwner) { resultAddress ->
                if (resultAddress != null) {
                    selectedAddress = resultAddress
                    updateAddressView()
                    calculateShippingCost()
                }
            }
    }

    private suspend fun loadInitialAddress() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val addressesCollection = db.collection("users").document(uid).collection("addresses")
            var snapshot = addressesCollection.whereEqualTo("isPrimary", true).limit(1).get().await()

            if (snapshot.isEmpty) {
                snapshot = addressesCollection.limit(1).get().await()
            }

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val addr = doc.toObject(Address::class.java)
                addr?.id = doc.id
                selectedAddress = addr
            } else {
                selectedAddress = null
            }
            updateAddressView()
            calculateShippingCost()

        } catch (e: Exception) {
            Log.e("PaymentFragment", "Gagal memuat alamat: ${e.message}")
        }
    }

    private fun handleOrderTypeChange(checkedId: Int) {
        isDelivery = (checkedId == R.id.rb_delivery)
        if (isDelivery) {
            if (selectedAddress == null) {
                viewLifecycleOwner.lifecycleScope.launch { loadInitialAddress() }
            } else {
                updateAddressView()
                calculateShippingCost()
            }
        } else {
            shippingCost = 0.0
            updateAddressView()
            updateTotals()
        }
    }

    private fun updateAddressView() {
        if (isDelivery) {
            layoutDeliveryInfo.visibility = View.VISIBLE
            layoutShippingCost.visibility = View.VISIBLE
            if (selectedAddress != null) {
                layoutAddressSelected.visibility = View.VISIBLE
                layoutAddressEmpty.visibility = View.GONE
                tvDeliveryAddress.text = "${selectedAddress!!.recipientName} (${selectedAddress!!.phoneNumber})\n${selectedAddress!!.fullAddress}"
            } else {
                layoutAddressSelected.visibility = View.GONE
                layoutAddressEmpty.visibility = View.VISIBLE
            }
        } else {
            layoutDeliveryInfo.visibility = View.GONE
            layoutShippingCost.visibility = View.GONE
        }
    }

    private fun calculateShippingCost() {
        if (!isDelivery || selectedAddress == null || selectedAddress!!.latLng == null) {
            shippingCost = 0.0
            updateTotals()
            return
        }

        tvShippingCost.text = "Menghitung..."
        fetchDirections(selectedAddress!!.latLng!!, storeLocation)
    }

    private fun fetchDirections(origin: LatLng, destination: LatLng) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val apiKey = "AIzaSyDIrN5Cr4dSpkpWwM4dbyt7DTaPf-2PLrw"
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"

            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonResponse = JSONObject(response)
                if (jsonResponse.getString("status") == "OK") {
                    val routes = jsonResponse.optJSONArray("routes")
                    if (routes != null && routes.length() > 0) {
                        val leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                        val distanceValue = leg.getJSONObject("distance").getInt("value") // meter

                        val cost = getCostFromDistance(distanceValue)

                        withContext(Dispatchers.Main) {
                            shippingCost = cost
                            updateTotals()
                        }
                    }
                } else {
                    Log.e("DirectionsAPI", "Status not OK: ${jsonResponse.getString("status")}")
                }
            } catch (e: Exception) {
                Log.e("DirectionsAPI", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    shippingCost = 0.0
                    tvShippingCost.text = "Gagal (Rp 0)"
                    updateTotals()
                }
            }
        }
    }

    private fun getCostFromDistance(distanceInMeters: Int): Double {
        val distanceInKm = distanceInMeters / 1000.0
        val cost = distanceInKm * 2500
        return when {
            cost < 8000 -> 8000.0
            cost > 50000 -> 50000.0
            else -> cost
        }
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
        tvShippingCost.text = formatCurrency(currentShipping)
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
        // Kembalikan UI ke kondisi awal (belum pilih voucher)
        tvSelectedVoucherName.text = "Pilih Voucher / Diskon"
        tvSelectedVoucherName.setTextColor(Color.BLACK)
        btnRemoveVoucher.visibility = View.GONE
        layoutDiscountInfo.visibility = View.GONE
        updateTotals()
    }

    private fun applyVoucherLogic(discount: Double, minPurchase: Double, title: String) {
        if (calculateSubtotal() >= minPurchase) {
            discountAmount = discount

            // Ubah UI menjadi Terpilih
            tvSelectedVoucherName.text = "$title (Hemat ${formatCurrency(discountAmount)})"
            tvSelectedVoucherName.setTextColor(Color.parseColor("#4CAF50")) // Warna Hijau
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

    // --- END LOGIKA VOUCHER ---

    private fun createOrder(isCashPayment: Boolean) {
        val currentCart = cartViewModel.cartList.value ?: return
        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val tokenPrefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)

        val orderType = if (isDelivery) "Delivery" else "Take Away"
        val addressToSave = if (isDelivery) selectedAddress else null

        val newOrder = Order(
            orderId = "TUKU-${System.currentTimeMillis()}",
            userEmail = prefs.getString("userEmail", "unknown@email.com") ?: "unknown",
            userName = prefs.getString("userName", "User") ?: "User",
            items = currentCart.toList(),
            totalAmount = finalTotalAmount,
            orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date()),
            status = if (isCashPayment) "Menunggu Pembayaran" else "Menunggu Konfirmasi",
            userToken = tokenPrefs.getString("fcm_token", "") ?: "",
            deliveryAddress = addressToSave,
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