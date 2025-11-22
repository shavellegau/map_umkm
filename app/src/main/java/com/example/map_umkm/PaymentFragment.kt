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
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CartItemAdapter
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Order
import com.example.map_umkm.viewmodel.CartViewModel
import com.google.firebase.firestore.FirebaseFirestore // <-- WAJIB IMPORT INI
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var jsonHelper: JsonHelper

    // UI Components
    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var btnPay: Button

    // UI Voucher
    private lateinit var etVoucher: EditText
    private lateinit var btnApplyVoucher: Button
    private lateinit var layoutDiscountInfo: RelativeLayout
    private lateinit var tvDiscountInfo: TextView

    private lateinit var cartAdapter: CartItemAdapter

    // Logic Variables
    private var discountAmount: Double = 0.0
    private var finalTotalAmount: Double = 0.0 // Menyimpan total akhir setelah diskon

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        jsonHelper = JsonHelper(requireContext())

        // Inisialisasi View
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_payment)
        rvOrderList = view.findViewById(R.id.rv_order_list)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)
        btnPay = view.findViewById(R.id.btnPay)

        // Init Voucher Views
        etVoucher = view.findViewById(R.id.et_voucher_code)
        btnApplyVoucher = view.findViewById(R.id.btn_apply_voucher)
        layoutDiscountInfo = view.findViewById(R.id.layout_discount_info)
        tvDiscountInfo = view.findViewById(R.id.tvDiscountInfo)

        setupRecyclerView()

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // --- TOMBOL BAYAR ---
        btnPay.setOnClickListener {
            if (cartViewModel.cartList.value.isNullOrEmpty()) {
                Toast.makeText(context, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                showPaymentChoiceDialog()
            }
        }

        // --- TOMBOL PAKAI VOUCHER ---
        btnApplyVoucher.setOnClickListener {
            val code = etVoucher.text.toString().trim().uppercase()
            if (code.isNotEmpty()) {
                checkVoucherToFirebase(code)
            } else {
                Toast.makeText(context, "Masukkan kode voucher dulu", Toast.LENGTH_SHORT).show()
            }
        }

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            calculateAndDisplayTotals() // Hitung ulang setiap item berubah
        }

        return view
    }

    // 🔥 CEK VOUCHER KE FIREBASE FIRESTORE 🔥
    private fun checkVoucherToFirebase(code: String) {
        val db = FirebaseFirestore.getInstance()

        // Cek di collection "vouchers"
        db.collection("vouchers").document(code)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Ambil data dari Firebase
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

        // Cek Syarat Minimal Belanja
        if (currentSubtotal >= minPurchase) {
            discountAmount = discount

            // Tampilkan Info Diskon
            layoutDiscountInfo.visibility = View.VISIBLE
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvDiscountInfo.text = "-${currencyFormat.format(discountAmount)}"

            Toast.makeText(context, "Voucher Berhasil Dipasang!", Toast.LENGTH_SHORT).show()

            // Update Angka Total Pembayaran
            calculateAndDisplayTotals()
        } else {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            Toast.makeText(context, "Min. belanja ${currencyFormat.format(minPurchase)}", Toast.LENGTH_SHORT).show()
            resetVoucher()
        }
    }

    private fun resetVoucher() {
        discountAmount = 0.0
        layoutDiscountInfo.visibility = View.GONE
        calculateAndDisplayTotals()
    }

    // --- HITUNG TOTAL (DENGAN DISKON) ---
    private fun calculateSubtotal(): Double {
        val currentCart = cartViewModel.cartList.value ?: emptyList()
        return currentCart.sumOf {
            val price = (if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0
            price * it.quantity
        }.toDouble()
    }

    private fun calculateAndDisplayTotals() {
        val subtotal = calculateSubtotal()
        val tax = subtotal * 0.11

        // Rumus: (Subtotal + Pajak) - Diskon
        var total = (subtotal + tax) - discountAmount
        if (total < 0) total = 0.0 // Jangan sampai minus

        finalTotalAmount = total // Simpan ke variabel global untuk Create Order

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
    }

    // --- PROSES ORDER ---
    private fun showPaymentChoiceDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_choice)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnQris: Button = dialog.findViewById(R.id.btnPayWithQris)
        val btnCash: Button = dialog.findViewById(R.id.btnPayWithCash)

        btnQris.setOnClickListener {
            dialog.dismiss()
            createOrder(isCashPayment = false)
        }
        btnCash.setOnClickListener {
            dialog.dismiss()
            createOrder(isCashPayment = true)
        }
        dialog.show()
    }

    private fun createOrder(isCashPayment: Boolean) {
        val currentCart = cartViewModel.cartList.value ?: return

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userName = prefs.getString("userName", "User") ?: "User"
        val userEmail = prefs.getString("userEmail", "unknown@email.com") ?: "unknown@email.com"

        val tokenPrefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val userTokenFCM = tokenPrefs.getString("fcm_token", "") ?: ""

        // PENTING: Gunakan finalTotalAmount yang sudah didiskon!
        val newOrder = Order(
            orderId = "TUKU-${System.currentTimeMillis()}",
            userEmail = userEmail,
            userName = userName,
            items = currentCart.toList(),
            totalAmount = finalTotalAmount, // <-- HARGA SETELAH DISKON
            orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date()),
            status = if (isCashPayment) "Menunggu Pembayaran" else "Menunggu Konfirmasi",
            userToken = userTokenFCM
        )

        if (jsonHelper.addOrder(newOrder)) {
            cartViewModel.clearCart()
            if (isCashPayment) {
                val action = PaymentFragmentDirections.actionPaymentFragmentToPaymentSuccessFragment(
                    paymentMethod = "CASH"
                )
                findNavController().navigate(action)
            } else {
                findNavController().navigate(R.id.action_paymentFragment_to_qrisFragment)
            }
        } else {
            Toast.makeText(context, "Gagal membuat pesanan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            mutableListOf(),
            onQuantityChanged = {
                // Jika user ubah qty barang, reset voucher agar dicek lagi min belanjanya
                resetVoucher()
                calculateAndDisplayTotals()
            },
            onDeleteItem = { product ->
                cartViewModel.deleteItem(product)
                resetVoucher()
            }
        )
        rvOrderList.layoutManager = LinearLayoutManager(requireContext())
        rvOrderList.adapter = cartAdapter
    }

    override fun onResume() {
        super.onResume()
        calculateAndDisplayTotals()
    }
}