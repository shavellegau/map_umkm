package com.example.map_umkm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.VoucherAdapter
import com.example.map_umkm.model.Voucher
import com.google.firebase.firestore.FirebaseFirestore

class VoucherSayaFragment : Fragment() {

    private lateinit var rvVoucher: RecyclerView
    private lateinit var adapter: VoucherAdapter
    private lateinit var btnBack: ImageView

    // Opsional: Tambahkan ProgressBar di XML jika mau loading,
    // tapi kalau tidak ada di XML, kita pakai Toast saja.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_voucher_saya, container, false)

        rvVoucher = view.findViewById(R.id.rvVoucher)
        btnBack = view.findViewById(R.id.btnBack)

        // Setup RecyclerView Kosong dulu
        rvVoucher.layoutManager = LinearLayoutManager(requireContext())

        // Tombol Back
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 🔥 AMBIL DATA DARI FIREBASE 🔥
        loadVouchersFromFirebase()

        return view
    }

    private fun loadVouchersFromFirebase() {
        val db = FirebaseFirestore.getInstance()

        // Ambil semua data dari koleksi "vouchers"
        // Kita bisa filter hanya voucher yang aktif (isActive == true)
        db.collection("vouchers")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                val voucherList = mutableListOf<Voucher>()

                for (document in documents) {
                    // Convert Data Firestore ke Model Voucher
                    val voucher = document.toObject(Voucher::class.java)
                    voucherList.add(voucher)
                }

                if (voucherList.isEmpty()) {
                    Toast.makeText(context, "Belum ada voucher tersedia", Toast.LENGTH_SHORT).show()
                }

                // Pasang ke Adapter
                setupAdapter(voucherList)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Toast.makeText(context, "Gagal memuat voucher", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupAdapter(vouchers: List<Voucher>) {
        adapter = VoucherAdapter(vouchers) { voucher ->
            // Aksi saat voucher diklik: Salin Kode
            copyToClipboard(voucher.code)
        }
        rvVoucher.adapter = adapter
    }

    private fun copyToClipboard(code: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Kode Voucher", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Kode $code disalin!", Toast.LENGTH_SHORT).show()
    }
}