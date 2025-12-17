package com.example.map_umkm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class PetaPilihLokasiActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvAlamat: TextView
    private lateinit var tvKoordinat: TextView
    private lateinit var btnKonfirmasi: Button
    private var centerLatLng: LatLng? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) getMyLocation()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta_pilih_lokasi)

        tvAlamat = findViewById(R.id.tvAlamatLengkap)
        tvKoordinat = findViewById(R.id.tvInfoJarak)
        btnKonfirmasi = findViewById(R.id.btn_konfirmasi_lokasi)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        btnKonfirmasi.setOnClickListener {
            if (centerLatLng != null) {
                val intent = Intent()
                intent.putExtra("LATITUDE", centerLatLng!!.latitude)
                intent.putExtra("LONGITUDE", centerLatLng!!.longitude)

                // Pastikan alamat tidak null
                val alamatFinal = tvAlamat.text.toString()
                intent.putExtra("ALAMAT", if (alamatFinal.contains("Memuat")) "Lokasi terpilih" else alamatFinal)

                setResult(RESULT_OK, intent)
                finish() // Tutup activity ini dan kembali ke Fragment
            } else {
                Toast.makeText(this, "Tunggu peta memuat lokasi...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        getMyLocation()

        val defaultLocation = LatLng(-6.175392, 106.827153)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

        mMap.setOnCameraIdleListener {
            centerLatLng = mMap.cameraPosition.target
            tvKoordinat.text = "Lat: ${centerLatLng?.latitude}, Lon: ${centerLatLng?.longitude}"
            centerLatLng?.let { getAddressFromLocation(it) }
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getAddressFromLocation(latLng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            // Gunakan thread background agar tidak lag (opsional, tapi disarankan)
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                tvAlamat.text = addresses[0].getAddressLine(0)
            } else {
                tvAlamat.text = "Alamat tidak ditemukan"
            }
        } catch (e: Exception) {
            tvAlamat.text = "Gagal memuat alamat"
        }
    }
}