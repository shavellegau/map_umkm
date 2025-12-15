package com.example.map_umkm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.R
import com.example.map_umkm.model.Cabang

// 🔥 Adapter Fleksibel: Menggunakan null untuk listener yang tidak digunakan (Default untuk Admin)
class CabangAdapter(
    private var daftarCabang: List<Cabang>,
    private val ketikaEditDiklik: ((Cabang) -> Unit)? = null, // Optional (untuk Admin)
    private val ketikaHapusDiklik: ((Cabang) -> Unit)? = null // Optional (untuk Admin)
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    // Default: Item Click Listener (untuk PilihCabangActivity)
    private var onItemClick: ((Cabang) -> Unit)? = null

    // 🔥 Constructor SEKUNDER untuk PilihCabangActivity (Hanya List dan Item Click)
    constructor(
        dataList: MutableList<Cabang>,
        klikItem: (Cabang) -> Unit
    ) : this(
        // Panggil Primary Constructor: daftarCabang, ketikaEditDiklik=null, ketikaHapusDiklik=null
        dataList,
        null,
        null
    ) {
        // Simpan listener klik item ke variabel lokal
        this.onItemClick = klikItem
    }

    // 1. CabangViewHolder
    inner class CabangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ID Admin dan Umum harus ada di layout item_admin_cabang.xml
        val tvNama: TextView = itemView.findViewById(R.id.tv_cabang_nama)
        val tvAlamat: TextView = itemView.findViewById(R.id.tv_cabang_alamat)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_cabang_status)
        // Tombol ini mungkin null di layout pengguna
        val btnEdit: Button? = itemView.findViewById(R.id.btn_edit_cabang)
        val btnHapus: Button? = itemView.findViewById(R.id.btn_delete_cabang)
    }

    // 2. onCreateViewHolder (Asumsi: Anda menggunakan layout item_admin_cabang yang memiliki tombol)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        val layoutResId = if (ketikaEditDiklik != null || ketikaHapusDiklik != null) {
            // Jika ada listener Admin, gunakan layout Admin
            R.layout.item_admin_cabang
        } else {
            // Jika tidak ada, asumsikan ini layout Pengguna/Pilih
            R.layout.item_cabang_user // 🔥 ASUMSI: Anda punya layout item_cabang_user.xml 🔥
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return CabangViewHolder(view)
    }

    // 3. onBindViewHolder
    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) {
        val cabang = daftarCabang[position]

        // Menetapkan Data
        holder.tvNama.text = cabang.nama
        holder.tvAlamat.text = cabang.alamat
        val teksStatus = "${cabang.statusBuka} (${cabang.jamBuka})"
        holder.tvStatus.text = teksStatus

        // Mengatur warna status (kode warna disederhanakan)
        val konteks = holder.itemView.context
        val idWarna = when (cabang.statusBuka.lowercase()) {
            "buka", "buka 24 jam" -> R.color.tuku_green // Ganti dengan ID warna Anda
            "tutup" -> R.color.tuku_red // Ganti dengan ID warna Anda
            else -> R.color.tuku_gray // Ganti dengan ID warna Anda
        }
        // holder.tvStatus.background.setTint(ContextCompat.getColor(konteks, idWarna))

        // 🔥 LOGIKA LISTENER 🔥
        if (ketikaEditDiklik != null && ketikaHapusDiklik != null) {
            // KASUS 1: ADMIN (menggunakan Primary Constructor)
            // PERBAIKAN: Menggunakan safe call '?.invoke()'
            holder.btnEdit?.setOnClickListener { ketikaEditDiklik?.invoke(cabang) }
            holder.btnHapus?.setOnClickListener { ketikaHapusDiklik?.invoke(cabang) }
            // Item view tidak merespons klik
            holder.itemView.setOnClickListener(null)

        } else if (onItemClick != null) {
            // KASUS 2: PENGGUNA/PILIH (menggunakan Secondary Constructor)
            holder.itemView.setOnClickListener { onItemClick?.invoke(cabang) }
            // Pastikan tombol admin disembunyikan jika layout yang digunakan adalah item_admin_cabang
            holder.btnEdit?.visibility = View.GONE
            holder.btnHapus?.visibility = View.GONE
        }
    }

    override fun getItemCount() = daftarCabang.size

    // 5. updateData
    fun updateData(daftarBaru: List<Cabang>) {
        daftarCabang = daftarBaru
        notifyDataSetChanged()
    }
}