package com.example.map_umkm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AdminOrdersAdapter
import com.example.map_umkm.data.JsonHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class AdminOrdersFragment : Fragment() {

    private lateinit var jsonHelper: JsonHelper
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter

    // Service Notifikasi
    private lateinit var fcmService: FCMService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)

        // 1. Init Helper & Service
        jsonHelper = JsonHelper(requireContext())
        fcmService = FCMService(requireContext())

        // 2. Init Views
        rvOrders = view.findViewById(R.id.rv_admin_orders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)

        setupRecyclerView()

        // 3. LOGIKA FAB (Tombol Bulat - Quick Action)
        val fabAction: FloatingActionButton = view.findViewById(R.id.fab_add_promo)
        fabAction.setOnClickListener {
            showPromoDialog() // Hanya untuk kirim info broadcast cepat
        }

        // 🔥 4. LOGIKA TOMBOL BARU (Kelola Voucher) 🔥
// 4. LOGIKA TOMBOL KELOLA VOUCHER
        val btnManageVoucher: Button = view.findViewById(R.id.btnManageVoucher)
        btnManageVoucher.setOnClickListener {

            // PERBAIKAN DI SINI: Gunakan 'fragment_container_overlay'
            parentFragmentManager.beginTransaction()
                .add(R.id.fragment_container_overlay, AdminVoucherFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    // --- FITUR: DIALOG PROMO BIASA (Broadcast) ---
    private fun showPromoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kirim Info Broadcast")
        builder.setMessage("Pesan ini dikirim ke semua user.")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputTitle = EditText(requireContext())
        inputTitle.hint = "Judul (Cth: Info Libur)"
        layout.addView(inputTitle)

        val inputBody = EditText(requireContext())
        inputBody.hint = "Isi Pesan"
        layout.addView(inputBody)

        builder.setView(layout)

        builder.setPositiveButton("Kirim") { _, _ ->
            val title = inputTitle.text.toString()
            val body = inputBody.text.toString()
            if (title.isNotEmpty()) {
                fcmService.sendNotification("promo", title, body)
                Toast.makeText(context, "Info Terkirim!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.show()
    }

    // --- LOGIKA RECYCLERVIEW PESANAN ---
    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter(
            emptyList(),
            onItemClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java).apply {
                    putExtra("ORDER_DATA", order)
                }
                startActivity(intent)
            },
            onConfirmPaymentClick = { order ->
                updateStatusAndNotify(order.orderId, "Menunggu Konfirmasi", order.userToken, "Status Pesanan", "Pembayaran sedang dicek Admin.")
            },
            onProsesClick = { order ->
                updateStatusAndNotify(order.orderId, "Diproses", order.userToken, "Pesanan Diproses", "Pesananmu sedang dibuat.")
            },
            onSelesaikanClick = { order ->
                updateStatusAndNotify(order.orderId, "Selesai", order.userToken, "Pesanan Selesai", "Silakan ambil pesananmu!")
            }
        )
        rvOrders.layoutManager = LinearLayoutManager(context)
        rvOrders.adapter = adapter
    }

    private fun updateStatusAndNotify(orderId: String, newStatus: String, token: String?, title: String, body: String) {
        jsonHelper.updateOrderStatus(orderId, newStatus)
        loadOrders()
        if (!token.isNullOrEmpty()) {
            fcmService.sendNotification(token, title, body)
        }
    }

    private fun loadOrders() {
        val allOrders = jsonHelper.getMenuData()?.orders?.sortedByDescending { it.orderDate } ?: emptyList()
        if (allOrders.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvOrders.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvOrders.visibility = View.VISIBLE
            adapter.updateData(allOrders)
        }
    }
}