package com.example.map_umkm

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class OrderDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // Sesuaikan ID dengan activity_order_detail.xml
        val tvType = findViewById<TextView>(R.id.tvTypeDetail)
        val tvPlace = findViewById<TextView>(R.id.tvPlaceDetail)
        val tvItems = findViewById<TextView>(R.id.tvItemsDetail)
        val tvPrice = findViewById<TextView>(R.id.tvPriceDetail)
        val tvDate = findViewById<TextView>(R.id.tvDateDetail)

        // Ambil data dari intent
        tvType.text = intent.getStringExtra("ORDER_TYPE")
        tvPlace.text = intent.getStringExtra("ORDER_PLACE")
        tvItems.text = intent.getStringExtra("ORDER_ITEMS")
        tvPrice.text = intent.getStringExtra("ORDER_PRICE")
        tvDate.text = intent.getStringExtra("ORDER_DATE")

        // Setup Toolbar dengan tombol back
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish() // kembali ke screen sebelumnya
        }
    }
}
