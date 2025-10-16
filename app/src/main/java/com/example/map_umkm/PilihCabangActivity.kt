package com.example.map_umkm

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

// data class Cabang tetap sama
data class Cabang(
    val nama: String,
    val alamat: String,
    val jamBuka: String,
    val statusBuka: String,
    val detail: String
)

class PilihCabangActivity : AppCompatActivity() {

    private lateinit var rvCabang: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var cabangAdapter: CabangAdapter
    private val daftarCabangLengkap = listOf(
        // Data cabang Anda tetap sama...
        Cabang("Kopi Tuku — Outlet Kedoya", "Jl. Kedoya Raya No.12, Kedoya, Kebon Jeruk, Jakarta Barat 11520", "07:00–22:00", "Buka", "Detail: +62 21 555-0101; WiFi, Parkir motor & mobil, Area smoking, Takeaway & Delivery."),
        Cabang("Kopi Tuku — Outlet Senayan", "Jl. Asia Afrika No.8, Gelora, Tanah Abang, Jakarta Pusat 10270", "07:30–22:30", "Buka", "Detail: +62 21 555-0102; WiFi, AC, Meja kerja, Layanan reservasi."),
        Cabang("Kopi Tuku — Outlet Pantai Indah Kapuk", "Jl. Marina Indah Blok B3 No.5, PIK, Penjaringan, Jakarta Utara 14470", "08:00–23:00", "Buka", "Detail: +62 21 555-0103; Parkir luas, Outdoor seating, Kids friendly."),
        Cabang("Kopi Tuku — Outlet Kemang", "Jl. Kemang Raya No.45, Bangka, Mampang Prapatan, Jakarta Selatan 12730", "08:00–01:00", "Buka", "Detail: +62 21 555-0104; Live music (weekend), WiFi, Vegan options."),
        Cabang("Kopi Tuku — Outlet Bogor", "Jl. Pajajaran No.210, Bogor Tengah, Bogor 16128", "07:00–21:00", "Buka", "Detail: +62 251 555-0105; Pemandangan taman, Area keluarga, Takeaway."),
        Cabang("Kopi Tuku — Outlet Bandung", "Jl. Dago Pakar No.60, Dago, Coblong, Bandung 40135", "07:30–22:00", "Buka", "Detail: +62 22 555-0106; WiFi, Parkir, Kopi spesial lokal."),
        Cabang("Kopi Tuku — Outlet Surabaya", "Jl. Raya Darmo No.88, Wonokromo, Surabaya 60241", "06:30–22:00", "Buka", "Detail: +62 31 555-0107; Sarapan pagi, Delivery via aplikasi, AC."),
        Cabang("Kopi Tuku — Outlet Yogyakarta", "Jl. Malioboro No.10, Gedongtengen, Yogyakarta 55271", "08:00–23:30", "Buka", "Detail: +62 274 555-0108; Cinderamata, Area indoor/outdoor, WiFi."),
        Cabang("Kopi Tuku — Outlet Semarang", "Jl. Pemuda No.120, Semarang Tengah, Semarang 50132", "07:00–21:30", "Buka", "Detail: +62 24 555-0109; Parkir, WiFi, Menu lokal."),
        Cabang("Kopi Tuku — Outlet Malang", "Jl. Ijen No.33, Klojen, Malang 65111", "07:00–22:00", "Buka", "Detail: +62 341 555-0110; Cozy corner, Outdoor heater (musim dingin)."),
        Cabang("Kopi Tuku — Outlet Solo", "Jl. Slamet Riyadi No.150, Laweyan, Surakarta 57141", "07:30–21:00", "Tutup", "Detail: +62 271 555-0111; Meeting room, WiFi, Paket catering."),
        Cabang("Kopi Tuku — Outlet 24 Jam Stasiun", "Jl. Stasiun No.1, Stasiun X, Jakarta Pusat 10110", "00:00–24:00", "Buka 24 Jam", "Detail: +62 21 555-0112; Buka 24 jam, WiFi, Takeaway cepat, Mesin kopi otomatis.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_cabang)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        rvCabang = findViewById(R.id.rv_cabang)
        etSearch = findViewById(R.id.et_search_cabang)

        setupToolbar(toolbar)
        setupRecyclerView()
        setupSearchListener()
    }

    private fun showConfirmationDialog(cabang: Cabang) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_confirmation, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnDelete) // gunakan btnDelete sebagai tombol "OK"

        tvTitle.text = "Konfirmasi Pilihan"
        tvMessage.text = "Anda memilih ${cabang.nama} sebagai lokasi pesanan Anda."

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Hapus background default dialog agar rounded bg dari XML terlihat
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.text = "OK"
        btnConfirm.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("NAMA_CABANG_TERPILIH", cabang.nama)
            setResult(Activity.RESULT_OK, resultIntent)
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }


    private fun setupToolbar(toolbar: MaterialToolbar) {
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        cabangAdapter = CabangAdapter(daftarCabangLengkap.toMutableList()) { cabang ->
            showConfirmationDialog(cabang)
        }
        rvCabang.layoutManager = LinearLayoutManager(this)
        rvCabang.adapter = cabangAdapter
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filter(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            daftarCabangLengkap
        } else {
            daftarCabangLengkap.filter { it.nama.contains(query, ignoreCase = true) || it.alamat.contains(query, ignoreCase = true) }
        }
        cabangAdapter.updateData(filteredList)
    }
}

class CabangAdapter(
    private var cabangList: MutableList<Cabang>,
    private val onPilihClick: (Cabang) -> Unit
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    class CabangViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namaCabang: TextView = view.findViewById(R.id.tv_nama_cabang)
        val alamatCabang: TextView = view.findViewById(R.id.tv_alamat_cabang)
        val jamBuka: TextView = view.findViewById(R.id.tv_jam_buka)
        val statusBuka: TextView = view.findViewById(R.id.tv_status_buka)
        val detailCabang: TextView = view.findViewById(R.id.tv_detail_cabang)
        val btnPilih: Button = view.findViewById(R.id.btn_pilih_cabang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cabang_tuku, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) {
        val cabang = cabangList[position]
        holder.namaCabang.text = cabang.nama
        holder.alamatCabang.text = cabang.alamat
        holder.jamBuka.text = cabang.jamBuka
        holder.statusBuka.text = cabang.statusBuka
        holder.detailCabang.text = cabang.detail
        val context = holder.itemView.context
        val (bgColor, textColor) = when (cabang.statusBuka) {
            "Buka", "Buka 24 Jam" -> R.color.green_status to Color.WHITE
            "Tutup" -> R.color.red_button to Color.WHITE
            else -> R.color.text_secondary to Color.WHITE
        }
        holder.statusBuka.background.setTint(ContextCompat.getColor(context, bgColor))
        holder.statusBuka.setTextColor(textColor)
        holder.btnPilih.setOnClickListener { onPilihClick(cabang) }
    }

    override fun getItemCount() = cabangList.size

    fun updateData(newList: List<Cabang>) {
        cabangList.clear()
        cabangList.addAll(newList)
        notifyDataSetChanged()
    }
}

