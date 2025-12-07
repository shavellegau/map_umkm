package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Address

class AddressAdapter(
    private var addresses: List<Address>,
    // [DIUBAH] Tambahkan listener baru untuk klik pada item
    private val onItemClick: (Address) -> Unit,
    private val onEdit: (Address) -> Unit,
    private val onDelete: (Address) -> Unit,
    private val onSetPrimary: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvIsPrimary: TextView = view.findViewById(R.id.tvIsPrimary)
        val tvRecipient: TextView = view.findViewById(R.id.tvRecipient)
        val tvAddressDetail: TextView = view.findViewById(R.id.tvAddressDetail)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val btnSetPrimary: TextView = view.findViewById(R.id.btnSetPrimary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = addresses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addresses[position]
        holder.tvLabel.text = address.label
        holder.tvRecipient.text = "${address.recipientName} (${address.phoneNumber})"
        holder.tvAddressDetail.text = address.fullAddress

        if (address.isPrimary) {
            holder.tvIsPrimary.visibility = View.VISIBLE
            holder.btnSetPrimary.visibility = View.GONE
        } else {
            holder.tvIsPrimary.visibility = View.GONE
            holder.btnSetPrimary.visibility = View.VISIBLE
        }

        // --- [BAGIAN YANG PALING PENTING] ---
        // Tambahkan listener pada seluruh item view
        holder.itemView.setOnClickListener {
            onItemClick(address)
        }
        // ------------------------------------

        holder.btnEdit.setOnClickListener { onEdit(address) }
        holder.btnDelete.setOnClickListener { onDelete(address) }
        holder.btnSetPrimary.setOnClickListener { onSetPrimary(address) }
    }

    fun updateData(newAddresses: List<Address>) {
        this.addresses = newAddresses
        notifyDataSetChanged()
    }
}
