package com.example.map_umkm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map_umkm.adapter.CabangAdapter
import com.example.map_umkm.model.Cabang
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

class PilihCabangActivity : AppCompatActivity() {

    private lateinit var rvCabang: RecyclerView
    private lateinit var etSearch: EditText

    private lateinit var adapter: CabangAdapter

    private val db = FirebaseFirestore.getInstance()
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_cabang)

        rvCabang = findViewById(R.id.rv_cabang)
        etSearch = findViewById(R.id.et_search_cabang)

        rvCabang.layoutManager = LinearLayoutManager(this)

        adapter = CabangAdapter(emptyList()) { cabang ->
            pilihCabang(cabang)
        }
        rvCabang.adapter = adapter

        cekLokasiUser()
    }

    private fun cekLokasiUser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    userLat = loc.latitude
                    userLng = loc.longitude
                    loadCabang()
                } else {
                    loadCabang()
                }
            }
        } else {
            loadCabang()
        }
    }

    private fun loadCabang() {
        db.collection("branches").get().addOnSuccessListener { result ->
            val list = mutableListOf<Cabang>()
            for (doc in result) {
                val lat = doc.getDouble("latitude") ?: 0.0
                val lng = doc.getDouble("longitude") ?: 0.0

                var jarakMeter = 0f
                if (userLat != 0.0) {
                    val res = FloatArray(1)
                    Location.distanceBetween(userLat, userLng, lat, lng, res)
                    jarakMeter = res[0]
                }

                val itemCabang = Cabang(
                    id = doc.id,
                    nama = doc.getString("nama") ?: "",
                    alamat = doc.getString("alamat") ?: "",
                    jamBuka = doc.getString("jamBuka") ?: "",
                    jamTutup = doc.getString("jamTutup") ?: "",
                    fasilitas = doc.getString("fasilitas") ?: "",
                    latitude = lat,
                    longitude = lng
                )
                itemCabang.jarakHitung = jarakMeter
                list.add(itemCabang)
            }

            list.sortBy { it.jarakHitung }

            adapter = CabangAdapter(list) { cabang ->
                showConfirmationDialog(cabang)
            }
            rvCabang.adapter = adapter
        }
    }

    private fun pilihCabang(cabang: Cabang) {
        showConfirmationDialog(cabang)
    }

    private fun showConfirmationDialog(cabang: Cabang) {
        val jarakInfo = if (userLat != 0.0) "(${String.format("%.1f km", (cabang.jarakHitung ?: 0f)/1000)})" else ""

        AlertDialog.Builder(this)
            .setTitle("Pilih Cabang")
            .setMessage("Pilih ${cabang.nama} $jarakInfo sebagai lokasi pemesanan?")
            .setPositiveButton("Ya") { _, _ ->
                val prefs = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("selectedBranchId", cabang.id)
                    putString("selectedBranchName", cabang.nama)
                    putString("selectedBranchLat", cabang.latitude.toString())
                    putString("selectedBranchLng", cabang.longitude.toString())
                    apply()
                }

                val resultIntent = Intent()
                resultIntent.putExtra("CABANG_ID", cabang.id)
                resultIntent.putExtra("CABANG_NAMA", cabang.nama)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}