package com.example.map_umkm

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log // Tambahkan Log untuk debugging
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.AdminOrdersAdapter
import com.example.map_umkm.model.Order
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AdminOrdersFragment : Fragment() {

    // UI Daftar Pesanan
    private lateinit var rvRecentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: AdminOrdersAdapter
    private lateinit var fabAction: FloatingActionButton

    // UI Statistik
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalUsers: TextView

    // Firebase & Listener
    private lateinit var fcmService: FCMService
    private val db = FirebaseFirestore.getInstance()
    private var orderListener: ListenerRegistration? = null
    private var statsListener1: ListenerRegistration? = null
    private var statsListener2: ListenerRegistration? = null
    private var statsListener3: ListenerRegistration? = null

    // [FIXED] Tambahkan lokasi toko sebagai properti
    private val storeLocation = LatLng(-6.2088, 106.8456)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)

        fcmService = FCMService(requireContext())

        // Inisialisasi semua view
        initializeViews(view)

        setupRecyclerView()
        fabAction.setOnClickListener { showPromoDialog() }

        return view
    }

    private fun initializeViews(view: View) {
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders)
        tvEmpty = view.findViewById(R.id.tv_admin_no_orders)
        // [FIXED] ID fabAction tidak ada di layout fragment_admin_orders.xml, jadi kita cari ID yang benar.
        // Asumsikan FAB ini untuk fungsi broadcast/promo. Jika tidak ada, baris ini bisa dihapus/dikomentari.
        // fabAction = view.findViewById(R.id.fab_add_promo) // ID ini tidak ada di layout

        tvTotalOrders = view.findViewById(R.id.tvTotalOrders)
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts)
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers)
    }


    override fun onResume() {
        super.onResume()
        startListeningForOrders()
        startListeningForStats()
    }

    override fun onPause() {
        super.onPause()
        orderListener?.remove()
        statsListener1?.remove()
        statsListener2?.remove()
        statsListener3?.remove()
    }

    private fun startListeningForOrders() {
        orderListener?.remove()
        orderListener = db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Order list listen failed.", e)
                    Toast.makeText(context, "Gagal memuat daftar pesanan.", Toast.LENGTH_LONG).show()
                    tvEmpty.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                val orders = snapshots?.toObjects(Order::class.java) ?: emptyList()

                if (orders.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvRecentOrders.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvRecentOrders.visibility = View.VISIBLE
                    adapter.updateData(orders)
                }
            }
    }

    private fun startListeningForStats() {
        statsListener1?.remove()
        statsListener1 = db.collection("orders")
            .whereIn("status", listOf("Menunggu Konfirmasi", "Menunggu Pembayaran"))
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.e("Firestore", "Stats 1 failed.", e); return@addSnapshotListener }
                val count = snapshots?.size() ?: 0
                if (isAdded) tvTotalOrders.text = count.toString()
            }

        statsListener2?.remove()
        statsListener2 = db.collection("orders")
            .whereEqualTo("status", "Diproses")
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.e("Firestore", "Stats 2 failed.", e); return@addSnapshotListener }
                val count = snapshots?.size() ?: 0
                if (isAdded) tvTotalProducts.text = count.toString()
            }

        statsListener3?.remove()
        statsListener3 = db.collection("orders")
            .whereEqualTo("status", "Selesai")
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.e("Firestore", "Stats 3 failed.", e); return@addSnapshotListener }
                val count = snapshots?.size() ?: 0
                if (isAdded) tvTotalUsers.text = count.toString()
            }
    }

    // [FIXED] Nama fungsi lebih jelas dan hanya untuk update status
    private fun updateStatus(orderId: String, newStatus: String) {
        db.collection("orders").document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener { Toast.makeText(context, "Status diubah menjadi $newStatus", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(context, "Gagal update: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    // [FIXED] Fungsi ini sekarang hanya untuk update status dan kirim notifikasi
    private fun updateStatusAndNotify(orderId: String, newStatus: String, token: String?, title: String, body: String) {
        db.collection("orders").document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Status diubah & notifikasi dikirim!", Toast.LENGTH_SHORT).show()
                if (!token.isNullOrEmpty()) {
                    fcmService.sendNotification(token, title, body)
                }
            }
            .addOnFailureListener { e -> Toast.makeText(context, "Gagal update: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    // [NEW] Fungsi untuk antar pesanan delivery
    private fun antarPesananDanNotifikasi(order: Order) {
        val userAddress = order.deliveryAddress
        // Pastikan ini pesanan delivery dan punya koordinat
        if (order.orderType != "Delivery" || userAddress?.latLng == null) {
            Toast.makeText(context, "Hanya pesanan 'Delivery' yang bisa diantar. Pesanan ini diselesaikan.", Toast.LENGTH_SHORT).show()
            updateStatus(order.orderId, "Selesai")
            return
        }

        // Jalankan di background thread
        lifecycleScope.launch(Dispatchers.IO) {
            val apiKey = "AIzaSyDIrN5Cr4dSpkpWwM4dbyt7DTaPf-2PLrw" // Ganti dengan API Key Anda
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${storeLocation.latitude},${storeLocation.longitude}&destination=${userAddress.latLng!!.latitude},${userAddress.latLng!!.longitude}&key=$apiKey"

            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                connection.disconnect()

                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.optJSONArray("routes")
                var durationText = "sekitar 15-30 menit" // Fallback
                if (routes != null && routes.length() > 0) {
                    val leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    durationText = leg.getJSONObject("duration").getString("text")
                }

                // Kembali ke Main thread untuk update UI & kirim notif
                withContext(Dispatchers.Main) {
                    val title = "Pesananmu sedang diantar!"
                    val body = "Estimasi tiba dalam $durationText."
                    updateStatusAndNotify(order.orderId, "Sedang Diantar", order.userToken, title, body)
                }
            } catch (e: Exception) {
                Log.e("AdminOrders", "Gagal fetch directions: ${e.message}")
                // Jika API gagal, tetap kirim notifikasi dengan estimasi fallback
                withContext(Dispatchers.Main) {
                    val title = "Pesananmu sedang diantar!"
                    val body = "Estimasi tiba dalam waktu dekat."
                    updateStatusAndNotify(order.orderId, "Sedang Diantar", order.userToken, title, body)
                }
            }
        }
    }


    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter(
            emptyList(),
            onItemClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                startActivity(intent)
            },
            onConfirmPaymentClick = { order ->
                updateStatus(order.orderId, "Menunggu Konfirmasi")
            },
            onProsesClick = { order ->
                updateStatus(order.orderId, "Diproses")
            },
            // [FIXED] Nama parameter disesuaikan dengan adapter
            onAntarPesananClick = { order ->
                antarPesananDanNotifikasi(order)
            }
        )

        rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        rvRecentOrders.adapter = adapter
    }

    private fun showPromoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kirim Info Broadcast")
        builder.setMessage("Pesan ini dikirim ke semua user (topic: promo).")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputTitle = EditText(requireContext())
        inputTitle.hint = "Judul"
        layout.addView(inputTitle)

        val inputBody = EditText(requireContext())
        inputBody.hint = "Isi pesan"
        layout.addView(inputBody)

        builder.setView(layout)

        builder.setPositiveButton("Kirim") { _, _ ->
            val title = inputTitle.text.toString()
            val body = inputBody.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(context, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            fcmService.sendNotification("promo", title, body)
            Toast.makeText(context, "Broadcast terkirim!", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }
}
