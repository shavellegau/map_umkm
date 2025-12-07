package com.example.map_umkm

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.OrderDetailAdapter
import com.example.map_umkm.model.Order
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // Ambil seluruh objek Order dari intent
        @Suppress("DEPRECATION")
        val order = intent.getParcelableExtra<Order>("ORDER_DATA")

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        if (order != null) {
            populateOrderDetails(order)
        } else {
            // Handle jika data order tidak ditemukan
            Toast.makeText(this, "Gagal memuat detail pesanan.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun populateOrderDetails(order: Order) {
        // --- Deklarasi Semua View ---
        val tvOrderId: TextView = findViewById(R.id.tvOrderId)
        val tvUserEmail: TextView = findViewById(R.id.tvUserEmail)
        val tvOrderDate: TextView = findViewById(R.id.tvOrderDate)

        // Rincian Invoice
        val tvInvSubtotal: TextView = findViewById(R.id.tvInvSubtotal)
        val tvInvTax: TextView = findViewById(R.id.tvInvTax)
        val layoutVoucher: LinearLayout = findViewById(R.id.layoutVoucher)
        val tvInvDiscount: TextView = findViewById(R.id.tvInvDiscount)
        val tvTotalPrice: TextView = findViewById(R.id.tvTotalPrice)

        // RecyclerView
        val rvOrderItems: RecyclerView = findViewById(R.id.rvOrderItems)

        // View untuk Info Pengiriman
        val layoutDelivery: LinearLayout = findViewById(R.id.layout_delivery)
        val tvDeliveryAddress: TextView = findViewById(R.id.tvDeliveryAddress)
        // -----------------------------

        // Atur format mata uang
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        // --- Isi Data ke View ---
        tvOrderId.text = "ID Pesanan: ${order.orderId}"
        tvUserEmail.text = "Pemesan: ${order.userName} (${order.userEmail})"
        tvOrderDate.text = order.orderDate

        // --- [FIXED] Logika untuk menampilkan Info Pengiriman ---
        if (order.orderType == "Delivery" && order.deliveryAddress != null) {
            layoutDelivery.visibility = View.VISIBLE
            val address = order.deliveryAddress
            tvDeliveryAddress.text = "${address.recipientName} (${address.phoneNumber})\n${address.fullAddress}"
        } else {
            layoutDelivery.visibility = View.GONE
        }
        // --------------------------------------------------------

        // Setup RecyclerView untuk menampilkan daftar produk
        val orderItemsAdapter = OrderDetailAdapter(order.items)
        rvOrderItems.layoutManager = LinearLayoutManager(this)
        rvOrderItems.adapter = orderItemsAdapter

        // --- Hitung dan Tampilkan Rincian Harga ---
        val subtotal = order.items.sumOf {
            ((if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0) * it.quantity.toDouble()
        }
        val tax = subtotal * 0.11
        // Ongkir sekarang sudah pasti ada di dalam data order jika tipenya Delivery
        val shippingCost = if (order.orderType == "Delivery") 10000.0 else 0.0
        val discount = (subtotal + tax + shippingCost) - order.totalAmount

        tvInvSubtotal.text = currencyFormat.format(subtotal)
        tvInvTax.text = currencyFormat.format(tax)

        if (discount > 0) {
            layoutVoucher.visibility = View.VISIBLE
            tvInvDiscount.text = "-${currencyFormat.format(discount)}"
        } else {
            layoutVoucher.visibility = View.GONE
        }

        tvTotalPrice.text = currencyFormat.format(order.totalAmount)
    }
}
