package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Voucher

class VoucherAdapter(
    private val vouchers: List<Voucher>,
    private val isForSelection: Boolean,
    private val onUseClick: (Voucher) -> Unit,
    private val onDetailClick: (Voucher) -> Unit
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    inner class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvVoucherTitle)
        val tvCode: TextView = itemView.findViewById(R.id.tvVoucherCode)
        val tvExpiry: TextView = itemView.findViewById(R.id.tvVoucherExpiry)
        val btnUse: Button = itemView.findViewById(R.id.btnUseVoucher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = vouchers[position]
        holder.tvTitle.text = voucher.title
        holder.tvCode.text = "Kode: ${voucher.code}"
        holder.tvExpiry.text = "Berlaku hingga ${voucher.expiryDate}"

        holder.btnUse.text = if (isForSelection) "Gunakan" else "Lihat Detail"

        holder.itemView.setOnClickListener {
            onDetailClick(voucher)
        }

        holder.btnUse.setOnClickListener {
            onUseClick(voucher)
        }
    }

    override fun getItemCount() = vouchers.size
}
