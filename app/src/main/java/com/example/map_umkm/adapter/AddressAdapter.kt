package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Address

class AddressAdapter(
    private val addresses: MutableList<Address>,
    private val onItemClick: (Address) -> Unit,
    private val onEditClick: (Address) -> Unit,
    private val onDeleteClick: (Address) -> Unit,
    private val onSetPrimaryClick: (Address) -> Unit,
    private val onUseClick: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvRecipient: TextView = view.findViewById(R.id.tvRecipient)
        val tvAddressDetail: TextView = view.findViewById(R.id.tvAddressDetail)
        val tvIsPrimary: TextView = view.findViewById(R.id.tvIsPrimary)
        val btnSetPrimary: TextView = view.findViewById(R.id.btnSetPrimary)

        val btnEdit: View = view.findViewById(R.id.btnEdit)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
        val btnUse: Button = view.findViewById(R.id.btnUseAddress)

        fun bind(address: Address) {
            tvLabel.text = address.label ?: "Alamat"
            tvRecipient.text =
                "${address.recipientName ?: "-"} | ${address.phoneNumber ?: "-"}"
            tvAddressDetail.text = address.fullAddress ?: "-"

            if (address.isPrimary) {
                tvIsPrimary.visibility = View.VISIBLE
                btnSetPrimary.visibility = View.GONE
            } else {
                tvIsPrimary.visibility = View.GONE
                btnSetPrimary.visibility = View.VISIBLE
            }

            itemView.setOnClickListener { onItemClick(address) }
            btnEdit.setOnClickListener { onEditClick(address) }
            btnDelete.setOnClickListener { onDeleteClick(address) }
            btnSetPrimary.setOnClickListener { onSetPrimaryClick(address) }
            btnUse.setOnClickListener { onUseClick(address) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_address, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(addresses[position])
    }

    override fun getItemCount(): Int = addresses.size

    fun updateData(newData: List<Address>) {
        addresses.clear()
        addresses.addAll(newData)
        notifyDataSetChanged()
    }
}
