package com.example.map_umkm

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.OrderItemsAdapter
import com.example.map_umkm.model.Order
import com.example.map_umkm.model.Product
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // PENTING: Pastikan Objek Order yang dikirim ke sini sudah menyertakan nilai discountAmount yang benar.
        val order = intent.getParcelableExtra<Order>("ORDER_DATA")
        val currency = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        // UI Reference
        val tvOrderId = findViewById<TextView>(R.id.tvOrderId)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val tvOrderDate = findViewById<TextView>(R.id.tvOrderDate)

        // Invoice components
        val tvInvSubtotal = findViewById<TextView>(R.id.tvInvSubtotal)
        val tvInvTax = findViewById<TextView>(R.id.tvInvTax)
        val tvInvDiscount = findViewById<TextView>(R.id.tvInvDiscount)
        val layoutVoucher = findViewById<LinearLayout>(R.id.layoutVoucher)
        val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)

        if (order != null) {

            // INFO ORDER
            tvOrderId.text = "ID Pesanan: ${order.orderId}"
            tvUserEmail.text = "Email Pengguna: ${order.userEmail}"
            tvOrderDate.text = "Tanggal Pesanan: ${order.orderDate}"

            // HITUNG SUBTOTAL
            val subtotal = order.items.sumOf { product: Product ->
                // Menggunakan operator Elvis (?:) untuk harga null safety
                val harga = if (product.selectedType == "hot") {
                    product.price_hot ?: 0
                } else {
                    product.price_iced ?: 0
                }
                harga * product.quantity
            }.toLong()

            // Pajak 11%
            val tax = (subtotal * 0.11).toLong()

            // Diskon/Voucher: Menggunakan 0L jika discountAmount null
            // Pastikan properti discountAmount di model Order Anda bertipe Double atau Long/Int.
            val discount = order.discountAmount?.toLong() ?: 0L

            // Perhitungan Total: Subtotal + Pajak - Diskon
            val total = subtotal + tax - discount

            // SET UI
            tvInvSubtotal.text = currency.format(subtotal)
            tvInvTax.text = currency.format(tax)
            tvTotalPrice.text = currency.format(total)

            // TAMPILKAN DISKON JIKA ADA
            if (discount > 0L) {
                // Tampilkan baris diskon
                layoutVoucher.visibility = LinearLayout.VISIBLE

                // Format diskon dengan tanda minus (-) di depan
                tvInvDiscount.text = "-${currency.format(discount)}"
            } else {
                // Sembunyikan baris diskon jika diskon 0
                layoutVoucher.visibility = LinearLayout.GONE
            }


            // LIST PRODUK
            val rvOrderItems = findViewById<RecyclerView>(R.id.rvOrderItems)
            rvOrderItems.layoutManager = LinearLayoutManager(this)
            rvOrderItems.adapter = OrderItemsAdapter(order.items)
        }

        // TOOLBAR
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }
}