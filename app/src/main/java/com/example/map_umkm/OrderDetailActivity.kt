package com.example.map_umkm

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout 
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

        
        val layoutVoucher: View = findViewById(R.id.layoutVoucher)

        val tvInvDiscount: TextView = findViewById(R.id.tvInvDiscount)
        val tvTotalPrice: TextView = findViewById(R.id.tvTotalPrice)
        val rvOrderItems: RecyclerView = findViewById(R.id.rvOrderItems)

        
        val layoutDelivery: View = findViewById(R.id.layout_delivery)

        val tvDeliveryAddress: TextView = findViewById(R.id.tvDeliveryAddress)
        val btnPesananDiterima: Button = findViewById(R.id.btn_pesanan_diterima)

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        tvOrderId.text = "ID Pesanan: ${order.orderId}"
        tvUserEmail.text = "Pemesan: ${order.userName} (${order.userEmail})"
        tvOrderDate.text = order.orderDate

        
        if (order.orderType == "Delivery" && order.deliveryAddress != null) {
            layoutDelivery.visibility = View.VISIBLE
            val address = order.deliveryAddress
            tvDeliveryAddress.text = "${address.recipientName} (${address.phoneNumber})\n${address.fullAddress}"
        } else {
            layoutDelivery.visibility = View.GONE
        }

        
        val orderItemsAdapter = OrderDetailAdapter(order.items)
        rvOrderItems.layoutManager = LinearLayoutManager(this)
        rvOrderItems.adapter = orderItemsAdapter

        
        val subtotal = order.items.sumOf {
            ((if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0) * it.quantity.toDouble()
        }
        val tax = subtotal * 0.11

        
        val subtotalPlusTax = subtotal + tax
        val shippingAndDiscount = order.totalAmount - subtotalPlusTax

        val discount = if(shippingAndDiscount < 0) -shippingAndDiscount else 0.0
        

        tvInvSubtotal.text = currencyFormat.format(subtotal)
        tvInvTax.text = currencyFormat.format(tax)

        
        if (discount > 0) {
            layoutVoucher.visibility = View.VISIBLE
            tvInvDiscount.text = "-${currencyFormat.format(discount)}"
        } else {
            layoutVoucher.visibility = View.GONE
        }

        tvTotalPrice.text = currencyFormat.format(order.totalAmount)

        
        if (order.status == "Sedang Diantar" || order.status == "Dikirim") {
            btnPesananDiterima.visibility = View.VISIBLE
            btnPesananDiterima.setOnClickListener {
                db.collection("orders").document(order.orderId)
                    .update("status", "Selesai")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pesanan telah diselesaikan. Terima kasih!", Toast.LENGTH_LONG).show()
                        btnPesananDiterima.visibility = View.GONE
                        
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