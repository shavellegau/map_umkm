package com.example.map_umkm

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout // Masih dipakai untuk layout lain jika perlu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.OrderDetailAdapter
import com.example.map_umkm.model.Order
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        @Suppress("DEPRECATION")
        val order = intent.getParcelableExtra<Order>("ORDER_DATA")

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (order != null) {
            populateOrderDetails(order)
        } else {
            Toast.makeText(this, "Gagal memuat detail pesanan.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun populateOrderDetails(order: Order) {
        val tvOrderId: TextView = findViewById(R.id.tvOrderId)
        val tvUserEmail: TextView = findViewById(R.id.tvUserEmail)
        val tvOrderDate: TextView = findViewById(R.id.tvOrderDate)
        val tvInvSubtotal: TextView = findViewById(R.id.tvInvSubtotal)
        val tvInvTax: TextView = findViewById(R.id.tvInvTax)

        // 🔥 PERBAIKAN 1: Ganti LinearLayout ke View (atau MaterialCardView)
        val layoutVoucher: View = findViewById(R.id.layoutVoucher)

        val tvInvDiscount: TextView = findViewById(R.id.tvInvDiscount)
        val tvTotalPrice: TextView = findViewById(R.id.tvTotalPrice)
        val rvOrderItems: RecyclerView = findViewById(R.id.rvOrderItems)

        // 🔥 PERBAIKAN 2: Ganti LinearLayout ke View (karena di XML ini adalah MaterialCardView)
        val layoutDelivery: View = findViewById(R.id.layout_delivery)

        val tvDeliveryAddress: TextView = findViewById(R.id.tvDeliveryAddress)
        val btnPesananDiterima: Button = findViewById(R.id.btn_pesanan_diterima)

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        tvOrderId.text = "ID Pesanan: ${order.orderId}"
        tvUserEmail.text = "Pemesan: ${order.userName} (${order.userEmail})"
        tvOrderDate.text = order.orderDate

        // Logika Tampilan Delivery
        if (order.orderType == "Delivery" && order.deliveryAddress != null) {
            layoutDelivery.visibility = View.VISIBLE
            val address = order.deliveryAddress
            tvDeliveryAddress.text = "${address.recipientName} (${address.phoneNumber})\n${address.fullAddress}"
        } else {
            layoutDelivery.visibility = View.GONE
        }

        // Setup RecyclerView
        val orderItemsAdapter = OrderDetailAdapter(order.items)
        rvOrderItems.layoutManager = LinearLayoutManager(this)
        rvOrderItems.adapter = orderItemsAdapter

        // Perhitungan Harga
        val subtotal = order.items.sumOf {
            ((if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0) * it.quantity.toDouble()
        }
        val tax = subtotal * 0.11

        // Dapatkan ongkir dan diskon dari totalAmount
        val subtotalPlusTax = subtotal + tax
        val shippingAndDiscount = order.totalAmount - subtotalPlusTax

        val discount = if(shippingAndDiscount < 0) -shippingAndDiscount else 0.0
        // val shippingCost = if(shippingAndDiscount > 0) shippingAndDiscount else 0.0 // (Opsional jika ingin ditampilkan)

        tvInvSubtotal.text = currencyFormat.format(subtotal)
        tvInvTax.text = currencyFormat.format(tax)

        // Logika Tampilan Voucher
        if (discount > 0) {
            layoutVoucher.visibility = View.VISIBLE
            tvInvDiscount.text = "-${currencyFormat.format(discount)}"
        } else {
            layoutVoucher.visibility = View.GONE
        }

        tvTotalPrice.text = currencyFormat.format(order.totalAmount)

        // Logika Tombol "Pesanan Diterima"
        if (order.status == "Sedang Diantar" || order.status == "Dikirim") {
            btnPesananDiterima.visibility = View.VISIBLE
            btnPesananDiterima.setOnClickListener {
                db.collection("orders").document(order.orderId)
                    .update("status", "Selesai")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pesanan telah diselesaikan. Terima kasih!", Toast.LENGTH_LONG).show()
                        btnPesananDiterima.visibility = View.GONE
                        // Opsional: Refresh activity atau finish()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyelesaikan pesanan. Coba lagi.", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            btnPesananDiterima.visibility = View.GONE
        }
    }
}