package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Cabang

class CabangAdapter(
    private var daftarCabang: List<Cabang>,
    private val listener: ((Cabang) -> Unit)? = null
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    inner class CabangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ID ini HARUS ada di file item_cabang_user.xml
        val tvNama: TextView = itemView.findViewById(R.id.tv_cabang_nama)
        val tvAlamat: TextView = itemView.findViewById(R.id.tv_cabang_alamat)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_cabang_status)
        val tvJarak: TextView? = itemView.findViewById(R.id.tv_jarak_cabang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        // Panggil layout khusus User
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cabang_user, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) {
        val cabang = daftarCabang[position]

        holder.tvNama.text = cabang.nama
        holder.tvAlamat.text = cabang.alamat

        // Menggunakan variabel statusBuka yang baru kita buat di Model
        holder.tvStatus.text = "${cabang.statusBuka} (${cabang.jamBuka})"

        // Tampilkan Jarak
        if (cabang.jarakHitung != null) {
            val km = String.format("%.1f km", cabang.jarakHitung!! / 1000)
            holder.tvJarak?.text = km
            holder.tvJarak?.visibility = View.VISIBLE
        } else {
            holder.tvJarak?.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            listener?.invoke(cabang)
        }
    }

    override fun getItemCount() = daftarCabang.size

    fun updateData(newData: List<Cabang>) {
        daftarCabang = newData
        notifyDataSetChanged()
    }
}