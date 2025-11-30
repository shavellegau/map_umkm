package com.example.map_umkm

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map_umkm.adapter.CartItemAdapter
import com.example.map_umkm.databinding.FragmentPaymentBinding
import com.example.map_umkm.model.Address
import com.example.map_umkm.model.Order
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartItemAdapter

    private var discountAmount: Double = 0.0
    private var shippingCost: Double = 0.0
    private var isDelivery: Boolean = false
    private var selectedAddress: String? = null
    private var finalTotalAmount: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Panggilan fungsi yang benar
        setupRecyclerView()
        setupListeners()

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            if (::cartAdapter.isInitialized) {
                cartAdapter.updateItems(cart)
            }
            calculateAndDisplayTotals()
        }

        binding.rbTakeAway.isChecked = true
    }

    override fun onResume() {
        super.onResume()
        // [FIXED] Saat kembali ke halaman ini, JIKA mode delivery aktif,        // muat ulang alamat utama untuk mengantisipasi perubahan.
        if (binding.rbDelivery.isChecked) {
            isDelivery = true
            loadPrimaryAddress()
        }
    }



    private fun setupListeners() {
        binding.toolbarPayment.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.btnPay.setOnClickListener {
            if (cartViewModel.cartList.value.isNullOrEmpty()) {
                Toast.makeText(context, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            } else if (isDelivery && selectedAddress.isNullOrEmpty()) {
                Toast.makeText(context, "Pilih alamat pengiriman terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                showPaymentChoiceDialog()
            }
        }

        binding.btnApplyVoucher.setOnClickListener {
            val code = binding.etVoucherCode.text.toString().trim().uppercase()
            if (code.isNotEmpty()) {
                checkVoucherToFirebase(code)
            } else {
                Toast.makeText(context, "Masukkan kode voucher dulu", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_delivery -> {
                    isDelivery = true
                    shippingCost = 10000.0
                    binding.layoutDeliveryInfo.visibility = View.VISIBLE
                    loadPrimaryAddress()
                }
                R.id.rb_take_away -> {
                    isDelivery = false
                    shippingCost = 0.0
                    selectedAddress = "Ambil di tempat"
                    binding.layoutDeliveryInfo.visibility = View.GONE
                    calculateAndDisplayTotals()
                }
            }
            resetVoucher()
        }

        binding.btnChangeAddress.setOnClickListener {
            findNavController().navigate(R.id.alamatFragment)
        }
    }

    // [FIXED] Fungsi ini dipindahkan ke luar setupListeners()
    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            mutableListOf(),
            onQuantityChanged = {
                resetVoucher()
                calculateAndDisplayTotals()
            },
            onDeleteItem = { product ->
                cartViewModel.deleteItem(product)
                resetVoucher()
                calculateAndDisplayTotals()
            }
        )
        binding.rvOrderList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrderList.adapter = cartAdapter
    }

    private fun loadPrimaryAddress() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("addresses")
            .whereEqualTo("uid", uid)
            .whereEqualTo("isPrimary", true)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!isAdded) return@addOnSuccessListener
                if (documents.isEmpty) {
                    selectedAddress = null
                    binding.tvDeliveryAddress.text = "Pilih atau tambah alamat dulu"
                } else {
                    val primaryAddress = documents.first().toObject(Address::class.java)
                    selectedAddress = "${primaryAddress.recipientName}\n${primaryAddress.fullAddress}"
                    binding.tvDeliveryAddress.text = selectedAddress
                }
                calculateAndDisplayTotals()
            }
    }

    private fun calculateAndDisplayTotals() {
        val subtotal = calculateSubtotal()
        val tax = subtotal * 0.11

        binding.layoutShippingCost.isVisible = isDelivery
        binding.tvShippingCost.text = formatCurrency(shippingCost)

        var total = (subtotal + tax + shippingCost) - discountAmount
        if (total < 0) total = 0.0
        finalTotalAmount = total

        binding.tvSubtotal.text = formatCurrency(subtotal)
        binding.tvTax.text = formatCurrency(tax)
        binding.tvTotalPayment.text = formatCurrency(total)
    }

    private fun createOrder(isCashPayment: Boolean) {
        val currentCart = cartViewModel.cartList.value ?: return

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userName = prefs.getString("userName", "User") ?: "User"
        val userEmail = prefs.getString("userEmail", "unknown@email.com") ?: "unknown@email.com"

        val tokenPrefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val userTokenFCM = tokenPrefs.getString("fcm_token", "") ?: ""

        val newOrder = Order(
            orderId = "TUKU-${System.currentTimeMillis()}",
            userEmail = userEmail,
            userName = userName,
            items = currentCart.toList(),
            totalAmount = finalTotalAmount,
            orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date()),
            status = if (isCashPayment) "Menunggu Pembayaran" else "Menunggu Konfirmasi",
            userToken = userTokenFCM,
            isDelivery = this.isDelivery,
            deliveryAddress = if (this.isDelivery) this.selectedAddress else "Ambil di tempat",
            shippingCost = this.shippingCost
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("orders")
            .document(newOrder.orderId)
            .set(newOrder)
            .addOnSuccessListener {
                cartViewModel.clearCart()
                val destination = if (isCashPayment) {
                    PaymentFragmentDirections.actionPaymentFragmentToPaymentSuccessFragment(paymentMethod = "CASH")
                } else {
                    PaymentFragmentDirections.actionPaymentFragmentToQrisFragment()
                }
                findNavController().navigate(destination)
                Toast.makeText(context, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan pesanan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun formatCurrency(amount: Double): String {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        currencyFormat.maximumFractionDigits = 0
        return currencyFormat.format(amount)
    }

    private fun calculateSubtotal(): Double {
        val currentCart = cartViewModel.cartList.value ?: emptyList()
        return currentCart.sumOf {
            val price = (if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0
            price * it.quantity
        }.toDouble()
    }

    private fun showPaymentChoiceDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_choice)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnQris: Button = dialog.findViewById(R.id.btnPayWithQris)
        val btnCash: Button = dialog.findViewById(R.id.btnPayWithCash)

        btnQris.setOnClickListener {
            dialog.dismiss()
            createOrder(isCashPayment = false) // QRIS
        }
        btnCash.setOnClickListener {
            dialog.dismiss()
            createOrder(isCashPayment = true) // Tunai
        }
        dialog.show()
    }

    private fun checkVoucherToFirebase(code: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("vouchers").document(code)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val minPurchase = document.getDouble("minPurchase") ?: 0.0
                    val discount = document.getDouble("discountAmount") ?: 0.0
                    val isActive = document.getBoolean("isActive") ?: true

                    if (isActive) {
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

    private fun applyVoucherLogic(discount: Double, minPurchase: Double) {
        val currentSubtotal = calculateSubtotal()
        if (currentSubtotal >= minPurchase) {
            discountAmount = discount
            binding.layoutDiscountInfo.visibility = View.VISIBLE
            binding.tvDiscountInfo.text = "-${formatCurrency(discountAmount)}"
            Toast.makeText(context, "Voucher Berhasil Dipasang!", Toast.LENGTH_SHORT).show()
            calculateAndDisplayTotals()
        } else {
            Toast.makeText(context, "Min. belanja ${formatCurrency(minPurchase)}", Toast.LENGTH_SHORT).show()
            resetVoucher()
        }
    }

    private fun resetVoucher() {
        discountAmount = 0.0
        binding.layoutDiscountInfo.visibility = View.GONE
        calculateAndDisplayTotals()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
