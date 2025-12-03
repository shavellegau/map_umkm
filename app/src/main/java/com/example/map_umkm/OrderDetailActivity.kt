package com.example.map_umkm

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.example.map_umkm.adapter.OrderItemsAdapter
import com.example.map_umkm.model.Order
import com.example.map_umkm.model.Product // Pastikan model ini diimpor
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // Ambil seluruh objek Order dari intent
        val order = intent.getParcelableExtra<Order>("ORDER_DATA")

        if (order != null) {
            // Deklarasikan TextView untuk informasi dasar pesanan
            val tvOrderId = findViewById<TextView>(R.id.tvOrderId)
            val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
            val tvOrderDate = findViewById<TextView>(R.id.tvOrderDate)
            val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)

            // Atur format mata uang
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

            // Tampilkan informasi pesanan
            tvOrderId.text = "ID Pesanan: ${order.orderId}"
            tvUserEmail.text = "Email Pengguna: ${order.userEmail}"
            tvOrderDate.text = "Tanggal Pesanan: ${order.orderDate}"
            tvTotalPrice.text = "Total Harga: ${currencyFormat.format(order.totalAmount)}"

            // Setup RecyclerView untuk menampilkan daftar produk
            val rvOrderItems = findViewById<RecyclerView>(R.id.rvOrderItems)
            val orderItemsAdapter = OrderItemsAdapter(order.items)
            rvOrderItems.layoutManager = LinearLayoutManager(this)
            rvOrderItems.adapter = orderItemsAdapter
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Pesanan"

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}