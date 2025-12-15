package com.example.map_umkm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.VoucherAdapter
import com.example.map_umkm.model.Voucher
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class VoucherSayaFragment : Fragment() {

    private lateinit var rvVoucher: RecyclerView
    private lateinit var adapter: VoucherAdapter
    private lateinit var btnBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_voucher_saya, container, false)

        rvVoucher = view.findViewById(R.id.rvVoucher)
        btnBack = view.findViewById(R.id.btnBack)

        rvVoucher.layoutManager = LinearLayoutManager(requireContext())

        // Gunakan Navigation Controller untuk Back
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        loadVouchersFromFirebase()

        return view
    }

    private fun loadVouchersFromFirebase() {
        val db = FirebaseFirestore.getInstance()

        // Ambil hanya voucher yang aktif
        db.collection("vouchers")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                val voucherList = mutableListOf<Voucher>()
                for (document in documents) {
                    // Pastikan field di Firestore sesuai dengan Model Voucher
                    val voucher = document.toObject(Voucher::class.java)
                    voucherList.add(voucher)
                }

                if (voucherList.isEmpty()) {
                    Toast.makeText(context, "Belum ada voucher tersedia", Toast.LENGTH_SHORT).show()
                }

                setupAdapter(voucherList)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Toast.makeText(context, "Gagal memuat voucher", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupAdapter(vouchers: List<Voucher>) {
        // Saat diklik, jangan langsung aksi, tapi BUKA BOTTOM SHEET dulu
        adapter = VoucherAdapter(vouchers) { voucher ->
            showVoucherDetailBottomSheet(voucher)
        }
        rvVoucher.adapter = adapter
    }

    // 🔥 MENAMPILKAN POP-UP SYARAT & KETENTUAN (BOTTOM SHEET) 🔥
    private fun showVoucherDetailBottomSheet(voucher: Voucher) {
        // 1. Setup Dialog
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_voucher, null)
        dialog.setContentView(view)

        // 2. Bind View (Pastikan ID ini ada di layout_bottom_sheet_voucher.xml)
        val tvTitle = view.findViewById<TextView>(R.id.tvSheetTitle)
        val tvCode = view.findViewById<TextView>(R.id.tvSheetCode)
        val tvTerms = view.findViewById<TextView>(R.id.tvSheetTerms)
        val btnAction = view.findViewById<Button>(R.id.btnSheetAction)

        // 3. Isi Data ke View
        tvTitle.text = voucher.title
        tvCode.text = voucher.code

        // Format Rupiah untuk Min. Belanja
        val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val minPurchaseStr = formatRp.format(voucher.minPurchase)

        // 4. Gabungkan Syarat & Ketentuan (Min Belanja + Tanggal + Deskripsi)
        val sb = StringBuilder()
        sb.append("• Minimal belanja: $minPurchaseStr\n")
        sb.append("• Berlaku sampai: ${voucher.expiryDate}")

        // Tambahkan deskripsi tambahan dari Admin jika ada (field baru)
        if (voucher.description.isNotEmpty()) {
            sb.append("\n• ${voucher.description}")
        }

        tvTerms.text = sb.toString()

        // 5. CEK ASAL USER (Logika Pintar)
        // Apakah user datang dari halaman Payment?
        val previousBackStackEntry = findNavController().previousBackStackEntry
        val previousDestinationId = previousBackStackEntry?.destination?.id

        // Pastikan ID 'paymentFragment' ini SAMA PERSIS dengan id di nav_graph.xml
        val isFromPayment = (previousDestinationId == R.id.paymentFragment)

        // Ubah Teks Tombol Sesuai Konteks
        if (isFromPayment) {
            btnAction.text = "Gunakan Voucher"
        } else {
            btnAction.text = "Salin Kode"
        }

        // 6. Aksi Tombol
        btnAction.setOnClickListener {
            if (isFromPayment) {
                // A. JIKA DARI PAYMENT: Kirim Kode balik ke PaymentFragment
                // Kita kirim string kodenya saja, nanti PaymentFragment cek lagi ke Firebase biar aman
                previousBackStackEntry?.savedStateHandle?.set("selectedVoucherCode", voucher.code)

                dialog.dismiss() // Tutup Pop-up
                findNavController().popBackStack() // Kembali ke PaymentFragment

                Toast.makeText(context, "Voucher ${voucher.code} Dipilih!", Toast.LENGTH_SHORT).show()
            } else {
                // B. JIKA DARI PROFIL: Salin ke Clipboard
                copyToClipboard(voucher.code)
                dialog.dismiss() // Tutup Pop-up
            }
        }

        dialog.show()
    }

    private fun copyToClipboard(code: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Kode Voucher", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Kode $code disalin!", Toast.LENGTH_SHORT).show()
    }
}