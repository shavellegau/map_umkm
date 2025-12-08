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

    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var btnPay: Button
    private lateinit var etVoucher: EditText
    private lateinit var btnApplyVoucher: Button
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
    private var shippingCost: Double = 0.0 // Default 0, akan dihitung
    private var selectedAddress: Address? = null
    private var isDelivery = false

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    // [BARU] Definisikan lokasi toko
    private val storeLocation = LatLng(-6.2088, 106.8456) // Contoh: Lokasi Toko Tuku

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        initializeViews(view)
        setupRecyclerView()
        setupListeners()
        setupAddressHandling()

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            updateTotals()
        }

        rbTakeAway.isChecked = true
        handleOrderTypeChange(R.id.rb_take_away)

        return view
    }

    override fun onResume() {
        super.onResume()
        if (isDelivery) {
            viewLifecycleOwner.lifecycleScope.launch {
                loadInitialAddress()
            }
        }
    }

    private fun setupAddressHandling() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Address>("selectedAddress")
            ?.observe(viewLifecycleOwner) { resultAddress ->
                selectedAddress = resultAddress
                updateAddressView()
                // [MODIFIKASI] Panggil kalkulasi ongkir setelah alamat dipilih
                calculateShippingCost()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Address>("selectedAddress")
            }
    }

    private suspend fun loadInitialAddress() {
        if (selectedAddress != null) {
            updateAddressView()
            calculateShippingCost()
            return
        }

        val uid = auth.currentUser?.uid ?: return
        try {
            val addressesCollection = db.collection("users").document(uid).collection("addresses")
            var snapshot = addressesCollection.whereEqualTo("isPrimary", true).limit(1).get().await()

            if (snapshot.isEmpty) {
                snapshot = addressesCollection.limit(1).get().await()
            }

            selectedAddress = if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                // Penting: Ambil juga ID dokumennya
                doc.toObject(Address::class.java)?.apply { id = doc.id }
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e("PaymentFragment", "Gagal memuat alamat awal: ${e.message}")
            selectedAddress = null
        }
        updateAddressView()
        calculateShippingCost() // Panggil kalkulasi ongkir
    }

    // --- FITUR BARU: HITUNG ONGKIR & JARAK ---
    private fun calculateShippingCost() {
        val userAddress = selectedAddress
        if (isDelivery && userAddress?.latLng != null) {
            // Tampilkan loading & panggil API
            tvShippingCost.text = "Menghitung..."
            fetchDirections(userAddress.latLng!!, storeLocation)
        } else {
            // Jika tidak delivery atau tidak ada alamat, ongkir 0
            shippingCost = 0.0
            updateTotals()
        }
    }

    private fun fetchDirections(origin: LatLng, destination: LatLng) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            // Ganti dengan API Key Anda yang sudah mengaktifkan Directions API
            val apiKey = "AIzaSyDIrN5Cr4dSpkpWwM4dbyt7DTaPf-2PLrw"
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"

            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                connection.disconnect()

                val jsonResponse = JSONObject(response)
                if (jsonResponse.getString("status") == "OK") {
                    val routes = jsonResponse.optJSONArray("routes")
                    if (routes != null && routes.length() > 0) {
                        val leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                        val distanceValue = leg.getJSONObject("distance").getInt("value") // Jarak dalam meter
                        val newShippingCost = getCostFromDistance(distanceValue)

                        withContext(Dispatchers.Main) {
                            shippingCost = newShippingCost
                            updateTotals() // Perbarui total setelah ongkir didapat
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DirectionsAPI", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    shippingCost = 0.0
                    tvShippingCost.text = "Gagal hitung"
                    updateTotals()
                }
            }
        }
    }

    private fun getCostFromDistance(distanceInMeters: Int): Double {
        val distanceInKm = distanceInMeters / 1000.0
        // Logika: Rp 2.500 per km, minimal Rp 8.000, maksimal Rp 50.000
        val cost = distanceInKm * 2500
        return when {
            cost < 8000 -> 8000.0
            cost > 50000 -> 50000.0
            else -> cost
        }
    }
    // --- AKHIR FITUR BARU ---


    private fun initializeViews(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_payment)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        rvOrderList = view.findViewById(R.id.rv_order_list)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)
        btnPay = view.findViewById(R.id.btnPay)
        etVoucher = view.findViewById(R.id.et_voucher_code)
        btnApplyVoucher = view.findViewById(R.id.btn_apply_voucher)
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
                Toast.makeText(context, "Silakan tambah atau pilih alamat pengiriman terlebih dahulu.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showPaymentChoiceDialog()
        }

        btnApplyVoucher.setOnClickListener {
            val code = etVoucher.text.toString().trim().uppercase()
            if (code.isNotEmpty()) checkVoucherToFirebase(code)
            else Toast.makeText(context, "Masukkan kode voucher dulu", Toast.LENGTH_SHORT).show()
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

    private fun handleOrderTypeChange(checkedId: Int) {
        isDelivery = (checkedId == R.id.rb_delivery)
        if (isDelivery) {
            viewLifecycleOwner.lifecycleScope.launch {
                loadInitialAddress()
            }
        } else {
            selectedAddress = null
            updateAddressView()
            calculateShippingCost() // Hitung ulang ongkir (jadi 0)
        }
    }

    private fun updateAddressView() {
        if (isDelivery) {
            layoutDeliveryInfo.visibility = View.VISIBLE
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
        }
        layoutShippingCost.visibility = if (isDelivery) View.VISIBLE else View.GONE
    }

    private fun updateTotals() {
        val subtotal = calculateSubtotal()
        val tax = subtotal * 0.11
        // Gunakan variabel shippingCost yang sudah dihitung
        val currentShippingCost = if (isDelivery) shippingCost else 0.0

        var total = (subtotal + tax + currentShippingCost) - discountAmount
        if (total < 0) total = 0.0
        finalTotalAmount = total

        tvSubtotal.text = formatCurrency(subtotal)
        tvTax.text = formatCurrency(tax)
        tvShippingCost.text = formatCurrency(currentShippingCost)
        tvTotalPayment.text = formatCurrency(total)
    }

    // ... (Fungsi lain seperti checkVoucher, createOrder, dll tetap sama)
    // ... Pastikan fungsi createOrder sekarang menyertakan `shippingCost` ke dalam `totalAmount`
    // ... dan menyimpan `selectedAddress` dengan benar ke dokumen Order.
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
    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount)
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
    private fun applyVoucherLogic(discount: Double, minPurchase: Double) {
        val currentSubtotal = calculateSubtotal()
        if (currentSubtotal >= minPurchase) {
            discountAmount = discount
            layoutDiscountInfo.visibility = View.VISIBLE
            tvDiscountInfo.text = "-${formatCurrency(discountAmount)}"
            Toast.makeText(context, "Voucher Berhasil Dipasang!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Min. belanja ${formatCurrency(minPurchase)}", Toast.LENGTH_SHORT).show()
            resetVoucher()
        }
        updateTotals()
    }
    private fun resetVoucher() {
        discountAmount = 0.0
        etVoucher.text.clear()
        layoutDiscountInfo.visibility = View.GONE
        updateTotals()
    }
    private fun calculateSubtotal(): Double {
        return cartViewModel.cartList.value?.sumOf {
            ((if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0) * it.quantity.toDouble()
        } ?: 0.0
    }
    private fun checkVoucherToFirebase(code: String) {
        db.collection("vouchers").document(code).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val minPurchase = document.getDouble("minPurchase") ?: 0.0
                    val discount = document.getDouble("discountAmount") ?: 0.0
                    if (document.getBoolean("isActive") == true) {
                        applyVoucherLogic(discount, minPurchase)
                    } else {
                        Toast.makeText(context, "Maaf, Voucher tidak aktif", Toast.LENGTH_SHORT).show()
                        resetVoucher()
                    }
                } else {
                    Toast.makeText(context, "Kode Voucher Tidak Ditemukan!", Toast.LENGTH_SHORT).show()
                    resetVoucher()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal cek voucher: ${it.message}", Toast.LENGTH_SHORT).show()
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

