package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Cabang

class AdminCabangAdapter(
    private var listCabang: List<Cabang>,
    private val onEditClick: (Cabang) -> Unit,
    private val onDeleteClick: (Cabang) -> Unit
) : RecyclerView.Adapter<AdminCabangAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tv_cabang_nama)
        val tvAlamat: TextView = view.findViewById(R.id.tv_cabang_alamat)
        val tvStatus: TextView = view.findViewById(R.id.tv_cabang_status)

        val btnEdit: Button = view.findViewById(R.id.btn_edit)
        val btnDelete: Button = view.findViewById(R.id.btn_hapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listCabang[position]
        holder.tvNama.text = item.nama
        holder.tvAlamat.text = item.alamat

        holder.tvStatus.text = "Jam: ${item.jamBuka} - ${item.jamTutup}"

        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = listCabang.size

    fun updateData(newData: List<Cabang>) {
        listCabang = newData
        notifyDataSetChanged()
    }
}