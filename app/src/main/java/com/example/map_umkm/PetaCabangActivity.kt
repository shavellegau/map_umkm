package com.example.map_umkm // SUDAH DIPERBAIKI

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.map_umkm.R // IMPORT PENTING
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

// Data Class Sederhana (Internal)
data class CabangMap(
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
    private var myLocation: Location? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan.", Toast.LENGTH_SHORT).show()
                ambilDataCabangDariFirebase()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta_cabang)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        cekIzinDanAmbilLokasi()
    }

    private fun cekIzinDanAmbilLokasi() {
        if (ContextCompat.checkSelfPermission(
                this,
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
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    myLocation = location
                }
                ambilDataCabangDariFirebase()
            }
        } catch (e: SecurityException) {
            Log.e("PetaLog", "Error permission: ${e.message}")
        }
    }

    private fun ambilDataCabangDariFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("branches").limit(100).get()
            .addOnSuccessListener { result ->
                if (isDestroyed || isFinishing) return@addOnSuccessListener

                mMap.clear()
                val boundsBuilder = LatLngBounds.Builder()
                var jumlahMarkerValid = 0

                var cabangTerdekat: CabangMap? = null
                var jarakTerdekatMeter = Float.MAX_VALUE

                for (document in result) {
                    val nama = document.getString("nama") ?: "Tanpa Nama"
                    val lat = document.getDouble("latitude")
                    val lng = document.getDouble("longitude")
                    val jam = document.getString("jamBuka") ?: "-"
                    val status = document.getString("statusBuka") ?: "Tutup"

                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        val cabang = CabangMap(document.id, nama, lat, lng, jam, status)

                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(nama)
                                .snippet("$status ($jam)")
                        )
                        boundsBuilder.include(LatLng(lat, lng))
                        jumlahMarkerValid++

                        if (myLocation != null) {
                            val hasilJarak = FloatArray(1)
                            Location.distanceBetween(
                                myLocation!!.latitude, myLocation!!.longitude,
                                lat, lng,
                                hasilJarak
                            )
                            if (hasilJarak[0] < jarakTerdekatMeter) {
                                jarakTerdekatMeter = hasilJarak[0]
                                cabangTerdekat = cabang
                            }
                        }
                    }
                }

                if (jumlahMarkerValid > 0) {
                    if (myLocation != null) {
                        boundsBuilder.include(LatLng(myLocation!!.latitude, myLocation!!.longitude))
                    }
                    try {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150))
                    } catch (e: Exception) {
                        // Ignore camera update error if layout not ready
                    }

                    if (cabangTerdekat != null) {
                        val jarakKm = String.format("%.1f", jarakTerdekatMeter / 1000)
                        Toast.makeText(this, "Terdekat: ${cabangTerdekat.nama} ($jarakKm km)", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}