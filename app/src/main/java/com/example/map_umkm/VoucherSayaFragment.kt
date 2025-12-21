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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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

    private val isForSelection: Boolean by lazy {
        findNavController().previousBackStackEntry?.destination?.id == R.id.paymentFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_voucher_saya, container, false)

        rvVoucher = view.findViewById(R.id.rvVoucher)
        btnBack = view.findViewById(R.id.btnBack)

        rvVoucher.layoutManager = LinearLayoutManager(requireContext())

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        loadVouchersFromFirebase()

        return view
    }

    private fun loadVouchersFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val voucherList = mutableListOf<Voucher>()

        db.collection("vouchers").whereEqualTo("isActive", true).get().addOnSuccessListener { globalDocs ->
            for (doc in globalDocs) {
                doc.toObject(Voucher::class.java)?.let { voucherList.add(it) }
            }

            db.collection("users").document(uid).collection("vouchers").get().addOnSuccessListener { privateDocs ->
                for (doc in privateDocs) {
                    val v = Voucher().apply {
                        title = doc.getString("title") ?: "Diskon Referral"
                        description = doc.getString("desc") ?: ""
                        code = doc.getString("code") ?: "REFERRAL"
                        discountAmount = doc.getDouble("discountAmount") ?: 0.0
                        isActive = true
                    }
                    voucherList.add(v)
                }
                setupAdapter(voucherList)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdapter(vouchers: List<Voucher>) {
        adapter = VoucherAdapter(
            vouchers,
            isForSelection,
            onUseClick = { voucher ->
                if (isForSelection) {
                    setFragmentResult("request_voucher", bundleOf("selected_voucher" to voucher))
                    findNavController().popBackStack()
                } else {
                    showVoucherDetailBottomSheet(voucher)
                }
            },
            onDetailClick = { voucher ->
                showVoucherDetailBottomSheet(voucher)
            }
        )
        rvVoucher.adapter = adapter
    }

    private fun showVoucherDetailBottomSheet(voucher: Voucher) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_voucher, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvSheetTitle)
        val tvCode = view.findViewById<TextView>(R.id.tvSheetCode)
        val tvTerms = view.findViewById<TextView>(R.id.tvSheetTerms)

        tvTitle.text = voucher.title
        tvCode.text = voucher.code

        val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val minPurchaseStr = formatRp.format(voucher.minPurchase)

        val sb = StringBuilder()
        sb.append("• Minimal belanja: $minPurchaseStr\n")
        sb.append("• Berlaku sampai: ${voucher.expiryDate}")

        if (voucher.description.isNotEmpty()) {
            sb.append("\n• ${voucher.description}")
        }

        tvTerms.text = sb.toString()

        dialog.show()
    }
}
