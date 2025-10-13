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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CartItemAdapter
import com.example.map_umkm.data.JsonHelper
import com.example.map_umkm.model.Order
import com.example.map_umkm.viewmodel.CartViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentFragment : Fragment() {

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var jsonHelper: JsonHelper

    private lateinit var rvOrderList: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotalPayment: TextView
    private lateinit var btnPay: Button // Menggunakan ID dari layout Anda: btnPay

    private lateinit var cartAdapter: CartItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        jsonHelper = JsonHelper(requireContext())

        // Inisialisasi semua View
        val backButton: Button = view.findViewById(R.id.btnBack)
        rvOrderList = view.findViewById(R.id.rv_order_list)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)
        btnPay = view.findViewById(R.id.btnPay)
        btnPay.text = "Bayar Sekarang" // Ubah teks tombol agar lebih jelas

        setupRecyclerView()

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Tombol ini sekarang akan menampilkan DIALOG POP-UP
        btnPay.setOnClickListener {
            if (cartViewModel.cartList.value.isNullOrEmpty()) {
                Toast.makeText(context, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                showPaymentChoiceDialog() // Tampilkan pop-up pilihan
            }
        }

        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            cartAdapter.updateItems(cart)
            calculateAndDisplayTotals()
        }

        return view
    }

    // Fungsi untuk menampilkan dialog pilihan pembayaran
    private fun showPaymentChoiceDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_choice) // Gunakan layout baru kita
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnQris: Button = dialog.findViewById(R.id.btnPayWithQris)
        val btnCash: Button = dialog.findViewById(R.id.btnPayWithCash)

        btnQris.setOnClickListener {
            dialog.dismiss()
            createOrder(isCashPayment = false) // Buat pesanan untuk QRIS
        }

        btnCash.setOnClickListener {
            dialog.dismiss()
            createOrder(isCashPayment = true) // Buat pesanan untuk Cash
        }

        dialog.show()
    }

    // Logika createOrder dipindahkan ke sini
    private fun createOrder(isCashPayment: Boolean) {
        val currentCart = cartViewModel.cartList.value
        if (currentCart.isNullOrEmpty()) {
            Toast.makeText(context, "Keranjang Anda kosong.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userName = prefs.getString("userName", "User") ?: "User"
        val userEmail = prefs.getString("userEmail", "unknown@email.com") ?: "unknown@email.com"

        val subtotal = currentCart.sumOf {
            val price = (if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0
            price * it.quantity
        }.toDouble()
        val totalAmount = subtotal * 1.11 // Pajak 11%

        val newOrder = Order(
            orderId = "TUKU-${System.currentTimeMillis()}",
            userEmail = userEmail,
            userName = userName,
            items = currentCart.toList(),
            totalAmount = totalAmount,
            orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date()),
            status = if (isCashPayment) "Menunggu Pembayaran" else "Menunggu Konfirmasi"
        )

        if (jsonHelper.addOrder(newOrder)) {
            cartViewModel.clearCart() // Kosongkan keranjang

            // Navigasi berdasarkan pilihan
            if (isCashPayment) {
                // Langsung ke halaman sukses untuk pembayaran tunai
                val action = PaymentFragmentDirections.actionPaymentFragmentToPaymentSuccessFragment(
                    paymentMethod = "CASH"
                )
                findNavController().navigate(action)
            } else {
                // Ke halaman QRIS untuk pembayaran QR
                findNavController().navigate(R.id.action_paymentFragment_to_qrisFragment)
            }
        } else {
            Toast.makeText(context, "Gagal membuat pesanan.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        calculateAndDisplayTotals()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            mutableListOf(),
            onQuantityChanged = { calculateAndDisplayTotals() },
            onDeleteItem = { product -> cartViewModel.deleteItem(product) }
        )
        rvOrderList.layoutManager = LinearLayoutManager(requireContext())
        rvOrderList.adapter = cartAdapter
    }

    private fun calculateAndDisplayTotals() {
        val currentCart = cartViewModel.cartList.value ?: emptyList()

        // [FIXED] Logika popBackStack() DIHAPUS dari sini karena menyebabkan crash navigasi.
        // if (currentCart.isEmpty() && isResumed) {
        //     findNavController().popBackStack()
        //     return
        // }

        val subtotal = currentCart.sumOf {
            val price = (if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0
            price * it.quantity
        }.toDouble()
        val tax = subtotal * 0.11
        val total = subtotal + tax
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
    }
}
