package com.example.aplikasikamu // GANTI dengan nama package aplikasi kamu

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

// Data Class Sederhana untuk menampung data sementara
data class Cabang(
    val id: String,
    val nama: String,
    val latitude: Double,
    val longitude: Double,
    val jamBuka: String,
    val statusBuka: String
)

class PetaCabangActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variabel untuk menyimpan lokasi User saat ini
    private var myLocation: Location? = null

    // Launcher untuk meminta izin lokasi secara popup
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk fitur terdekat", Toast.LENGTH_SHORT).show()
                // Tetap load data meskipun tanpa lokasi user (jarak tidak akan dihitung)
                ambilDataCabangDariFirebase()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta_cabang) // Pastikan nama layout benar

        // Inisialisasi Lokasi Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inisialisasi Peta
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Setup UI Peta
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // Cek Izin Lokasi & Ambil Lokasi User Dulu
        cekIzinDanAmbilLokasi()
    }

    private fun cekIzinDanAmbilLokasi() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getMyLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getMyLocation() {
        try {
            // Aktifkan titik biru (Blue Dot) lokasi user di peta
            mMap.isMyLocationEnabled = true

            // Ambil koordinat GPS terakhir
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    myLocation = location
                    Log.d("PetaLog", "Lokasi user ditemukan: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.w("PetaLog", "Lokasi user null (Mungkin GPS Emulator belum diset)")
                    Toast.makeText(this, "Set lokasi di Emulator agar jarak terdeteksi!", Toast.LENGTH_LONG).show()
                }

                // Setelah mencoba ambil lokasi (dapat atau tidak), BARU ambil data Firebase
                ambilDataCabangDariFirebase()
            }
        } catch (e: SecurityException) {
            Log.e("PetaLog", "Error permission: ${e.message}")
        }
    }

    private fun ambilDataCabangDariFirebase() {
        val db = FirebaseFirestore.getInstance()

        // Ambil data (Limit 100 agar ringan di Emulator)
        db.collection("branches")
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                // Safety check: Jika activity sudah ditutup user, hentikan proses biar ga crash
                if (isDestroyed || isFinishing) return@addOnSuccessListener

                if (result.isEmpty) {
                    Toast.makeText(this, "Data cabang kosong", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                mMap.clear() // Hapus marker lama
                val boundsBuilder = LatLngBounds.Builder()

                // Variabel logika "Mencari Juara Terdekat"
                var cabangTerdekat: Cabang? = null
                var jarakTerdekatMeter = Float.MAX_VALUE // Nilai awal Infinity
                var jumlahMarkerValid = 0

                for (document in result) {
                    try {
                        // Parsing manual (Lebih aman dari error tipe data)
                        val nama = document.getString("nama") ?: "Tanpa Nama"
                        val lat = document.getDouble("latitude")
                        val lng = document.getDouble("longitude")
                        val jam = document.getString("jamBuka") ?: "-"
                        val status = document.getString("statusBuka") ?: "Tutup"

                        // Pastikan koordinat valid (bukan null dan bukan 0.0)
                        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                            val cabang = Cabang(document.id, nama, lat, lng, jam, status)

                            // 1. Tampilkan Marker
                            val marker = mMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(lat, lng))
                                    .title(nama)
                                    .snippet("Status: $status")
                            )
                            boundsBuilder.include(LatLng(lat, lng))
                            jumlahMarkerValid++

                            // 2. HITUNG JARAK (Jika lokasi user diketahui)
                            if (myLocation != null) {
                                val hasilJarak = FloatArray(1)
                                Location.distanceBetween(
                                    myLocation!!.latitude, myLocation!!.longitude, // User
                                    lat, lng, // Cabang
                                    hasilJarak // Output
                                )
                                val jarakIni = hasilJarak[0]

                                // Bandingkan: Apakah ini lebih dekat dari rekor sebelumnya?
                                if (jarakIni < jarakTerdekatMeter) {
                                    jarakTerdekatMeter = jarakIni
                                    cabangTerdekat = cabang

                                    // (Opsional) Beri warna beda untuk marker calon pemenang?
                                    // marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PetaLog", "Skip data rusak: ${e.message}")
                    }
                }

                // --- FINISHING ---
                if (jumlahMarkerValid > 0) {
                    try {
                        // Masukkan juga lokasi user ke kamera agar terlihat satu layar
                        if (myLocation != null) {
                            boundsBuilder.include(LatLng(myLocation!!.latitude, myLocation!!.longitude))
                        }

                        // Geser kamera otomatis
                        val bounds = boundsBuilder.build()
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)) // 150 padding

                        // Tampilkan Hasil Terdekat
                        if (cabangTerdekat != null && myLocation != null) {
                            val jarakKm = String.format("%.1f", jarakTerdekatMeter / 1000)
                            Toast.makeText(this, "Terdekat: ${cabangTerdekat.nama} ($jarakKm km)", Toast.LENGTH_LONG).show()
                        }

                    } catch (e: Exception) {
                        Log.w("PetaLog", "Gagal auto-zoom (Layar belum siap): ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal koneksi database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}