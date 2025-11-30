package com.example.map_umkm

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.OrderDetailAdapter
import com.example.map_umkm.model.Order
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var order: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        order = intent.getParcelableExtra("ORDER_DATA") ?: run {
            finish() // Jika tidak ada data order, tutup activity
            return
        }

        // Inisialisasi Views
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val tvOrderId: TextView = findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = findViewById(R.id.tvOrderDate)
        val tvUserEmail: TextView = findViewById(R.id.tvUserEmail)
        val rvItems: RecyclerView = findViewById(R.id.rvOrderItems)
        val tvInvSubtotal: TextView = findViewById(R.id.tvInvSubtotal)
        val tvInvTax: TextView = findViewById(R.id.tvInvTax)
        val tvTotalPrice: TextView = findViewById(R.id.tvTotalPrice)

        // ===================================
        //  UI BARU UNTUK INFO DELIVERY
        // ===================================
        val layoutDelivery: LinearLayout = findViewById(R.id.layout_delivery)
        val tvDeliveryAddress: TextView = findViewById(R.id.tvDeliveryAddress)
        val layoutVoucher: LinearLayout = findViewById(R.id.layoutVoucher)
        val tvInvDiscount: TextView = findViewById(R.id.tvInvDiscount)
        // ===================================

        toolbar.setNavigationOnClickListener { finish() }

        // Format Mata Uang
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        currencyFormat.maximumFractionDigits = 0

        // Set Data ke UI
        tvOrderId.text = "ID: ${order.orderId}"
        tvOrderDate.text = order.orderDate
        tvUserEmail.text = "Pemesan: ${order.userName} (${order.userEmail})"

        // Setup RecyclerView
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = OrderDetailAdapter(order.items)

        // ===================================
        //  LOGIKA TAMPILAN DELIVERY & VOUCHER
        // ===================================
        if (order.isDelivery) {
            layoutDelivery.visibility = View.VISIBLE
            tvDeliveryAddress.text = order.deliveryAddress ?: "Alamat tidak tersedia"
        } else {
            layoutDelivery.visibility = View.GONE
        }

        // Kalkulasi subtotal dari item
        val subtotal = order.items.sumOf {
            val price = (if (it.selectedType == "iced") it.price_iced else it.price_hot) ?: 0
            price.toDouble() * it.quantity
        }
        val tax = subtotal * 0.11

        tvInvSubtotal.text = currencyFormat.format(subtotal)
        tvInvTax.text = currencyFormat.format(tax)

        // Cek apakah ada diskon (total akhir < subtotal + tax + ongkir)
        val expectedTotal = subtotal + tax + order.shippingCost
        val discount = expectedTotal - order.totalAmount
        if (discount > 0.1) { // Diberi toleransi 0.1 untuk error pembulatan
            layoutVoucher.visibility = View.VISIBLE
            tvInvDiscount.text = "-${currencyFormat.format(discount)}"
        } else {
            layoutVoucher.visibility = View.GONE
        }
        // ===================================

        tvTotalPrice.text = currencyFormat.format(order.totalAmount)
    }
}
