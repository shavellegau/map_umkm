package com.example.map_umkm

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CartItemAdapter // Menggunakan adapter yang benar
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
    private lateinit var btnPay: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        jsonHelper = JsonHelper(requireContext())

        val backButton = view.findViewById<Button>(R.id.btnBack)
        rvOrderList = view.findViewById(R.id.rv_order_list)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotalPayment = view.findViewById(R.id.tvTotalPayment)
        btnPay = view.findViewById(R.id.btnPay)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val currentCart = cartViewModel.cartList.value ?: mutableListOf()
        // Menggunakan CartItemAdapter yang benar
        val adapter = CartItemAdapter(currentCart,
            onQuantityChanged = { calculateAndDisplayTotals() },
            onDeleteItem = { product ->
                // [FIXED] Menggunakan fungsi yang benar dari CartViewModel, yaitu `deleteItem`
                cartViewModel.deleteItem(product)
            }
        )
        rvOrderList.layoutManager = LinearLayoutManager(requireContext())
        rvOrderList.adapter = adapter

        calculateAndDisplayTotals()

        btnPay.setOnClickListener {
            processPayment()
        }

        // Observer untuk mengupdate adapter jika ada item yang dihapus
        cartViewModel.cartList.observe(viewLifecycleOwner) { cart ->
            // Cara yang benar untuk mengupdate RecyclerView setelah LiveData berubah
            (rvOrderList.adapter as? CartItemAdapter)?.notifyDataSetChanged()
            calculateAndDisplayTotals()
            if (cart.isEmpty()) {
                // Jika keranjang kosong (misal setelah item terakhir dihapus), kembali
                Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        return view
    }

    private fun processPayment() {
        val currentCart = cartViewModel.cartList.value ?: emptyList()
        if (currentCart.isEmpty()) {
            Toast.makeText(context, "Keranjang Anda kosong.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = requireActivity().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("userEmail", "unknown@email.com") ?: "unknown@email.com"
        val userName = prefs.getString("userName", "User") ?: "User"

        val totalAmount = (tvTotalPayment.text.toString().replace("[^\\d]".toRegex(), "")).toDoubleOrNull() ?: 0.0

        val newOrder = Order(
            orderId = UUID.randomUUID().toString(),
            userEmail = userEmail,
            userName = userName,
            items = currentCart.toList(),
            totalAmount = totalAmount / 100, // Dibagi 100 karena format Rupiah biasanya menambahkan dua nol
            orderDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date()),
            status = "Menunggu Konfirmasi"
        )

        val success = jsonHelper.addOrder(newOrder)

        if (success) {
            Toast.makeText(context, "Pembayaran berhasil! Pesanan sedang menunggu konfirmasi.", Toast.LENGTH_LONG).show()
            cartViewModel.clearCart()
            findNavController().popBackStack()
        } else {
            Toast.makeText(context, "Gagal memproses pesanan. Coba lagi.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateAndDisplayTotals() {
        val currentCart = cartViewModel.cartList.value ?: emptyList()
        val subtotal = currentCart.sumOf {
            val price = if (it.selectedType == "iced") it.price_iced else it.price_hot
            price * it.quantity
        }.toDouble()
        val tax = subtotal * 0.1
        val total = subtotal + tax

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvSubtotal.text = currencyFormat.format(subtotal)
        tvTax.text = currencyFormat.format(tax)
        tvTotalPayment.text = currencyFormat.format(total)
    }
}
