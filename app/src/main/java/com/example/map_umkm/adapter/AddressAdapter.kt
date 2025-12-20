package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Address

class AddressAdapter(
    private var addresses: List<Address>,
    // Callback Action
    private val onItemClick: (Address) -> Unit,
    private val onEditClick: (Address) -> Unit,
    private val onDeleteClick: (Address) -> Unit,
    private val onSetPrimaryClick: (Address) -> Unit,
    private val onUseClick: (Address) -> Unit // <--- BARU: Callback tombol Gunakan
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvRecipient: TextView = view.findViewById(R.id.tvRecipient)
        val tvAddressDetail: TextView = view.findViewById(R.id.tvAddressDetail)
        val tvIsPrimary: TextView = view.findViewById(R.id.tvIsPrimary)
        val btnSetPrimary: TextView = view.findViewById(R.id.btnSetPrimary)

        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val btnUse: Button = view.findViewById(R.id.btnUseAddress) // <--- Tombol Gunakan

        fun bind(address: Address) {
            tvLabel.text = address.label
            tvRecipient.text = "${address.recipientName} (${address.phoneNumber})"

            // Gabungkan alamat lengkap + detail + notes
            val fullDetails = StringBuilder(address.fullAddress)
            if (address.details.isNotEmpty()) fullDetails.append(", ${address.details}")
            if (address.notes.isNotEmpty()) fullDetails.append(" (${address.notes})")
            tvAddressDetail.text = fullDetails.toString()

            if (address.isPrimary) {
                tvIsPrimary.visibility = View.VISIBLE
                btnSetPrimary.visibility = View.GONE
            } else {
                tvIsPrimary.visibility = View.GONE
                // Opsional: Sembunyikan tombol 'Set Utama' jika tombol 'Gunakan' sudah ada biar ga penuh
                btnSetPrimary.visibility = View.VISIBLE
            }

            // Klik item
            itemView.setOnClickListener { onItemClick(address) }

            // Klik Tombol Aksi
            btnEdit.setOnClickListener { onEditClick(address) }
            btnDelete.setOnClickListener { onDeleteClick(address) }
            btnSetPrimary.setOnClickListener { onSetPrimaryClick(address) }

            // Klik tombol Gunakan
            btnUse.setOnClickListener { onUseClick(address) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = addresses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(addresses[position])
    }

    fun updateData(newData: List<Address>) {
        addresses = newData
        notifyDataSetChanged()
    }
}