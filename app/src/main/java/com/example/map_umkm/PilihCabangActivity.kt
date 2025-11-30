package com.example.map_umkm

import android.app.Activity
import android.content.Context // 🔥 WAJIB: Tambahkan import Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.map_umkm.adapter.CabangAdapter
import com.example.map_umkm.model.Cabang

class PilihCabangActivity : AppCompatActivity() {

    private lateinit var rvCabang: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var cabangAdapter: CabangAdapter

    private val db = FirebaseFirestore.getInstance()
    private var cabangListener: ListenerRegistration? = null
    private var daftarCabangLengkap = mutableListOf<Cabang>()

    // --- Siklus Hidup Activity ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_cabang)

        // Inisialisasi View
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        rvCabang = findViewById(R.id.rv_cabang)
        etSearch = findViewById(R.id.et_search_cabang)

        // Setup
        setupToolbar(toolbar)
        setupRecyclerView()
        setupSearchListener()

        // Mulai memuat data
        loadCabangFromFirebase()
    }

    override fun onDestroy() {
        super.onDestroy()
        cabangListener?.remove()
    }

    // --- Data dan Loading ---

    private fun loadCabangFromFirebase() {
        cabangListener?.remove()

        cabangListener = db.collection("branches")
            .orderBy("nama")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("CabangActivity", "Listen failed.", e)
                    Toast.makeText(this, "Gagal memuat data cabang.", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    daftarCabangLengkap.clear()

                    val newCabangList = snapshots.toObjects(Cabang::class.java)

                    for ((index, document) in snapshots.documents.withIndex()) {
                        newCabangList[index].id = document.id
                    }

                    daftarCabangLengkap.addAll(newCabangList)
                    filter(etSearch.text.toString())
                }
            }
    }

    // --- UI Setup ---

    private fun setupToolbar(toolbar: MaterialToolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }
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

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            daftarCabangLengkap
        } else {
            daftarCabangLengkap.filter {
                it.nama.contains(query, ignoreCase = true) || it.alamat.contains(query, ignoreCase = true)
            }
        }
        cabangAdapter.updateData(filteredList.toMutableList())
    }

    // --- Dialog dan Hasil ---

    private fun showConfirmationDialog(cabang: Cabang) {
        AlertDialog.Builder(this)
            .setTitle("Pilih Cabang")
            .setMessage("Apakah Anda yakin ingin memilih cabang ${cabang.nama}?")
            .setPositiveButton("Pilih") { dialog, _ ->

                // 🔥 PERUBAHAN UTAMA: Menyimpan data ke SharedPreferences 🔥
                val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("selectedBranchId", cabang.id)
                    putString("selectedBranchName", cabang.nama)
                    apply() // Terapkan perubahan
                }

                // Mengirim hasil kembali ke HomeFragment
                val resultIntent = Intent()
                resultIntent.putExtra("CABANG_ID", cabang.id)
                resultIntent.putExtra("CABANG_NAMA", cabang.nama) // Kirim nama yang disimpan
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}