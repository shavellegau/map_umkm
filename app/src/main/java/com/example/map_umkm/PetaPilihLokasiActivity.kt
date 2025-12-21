package com.example.map_umkm

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class PetaPilihLokasiActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnConfirm: Button

    private var selectedLat: Double = -6.200000
    private var selectedLng: Double = 106.816666
    private var selectedAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta_pilih_lokasi)

        btnConfirm = findViewById(R.id.btn_confirm_location)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnConfirm.setOnClickListener {
            if (selectedAddress.isEmpty()) {
                Toast.makeText(this, "Sedang memuat alamat...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent()
            resultIntent.putExtra("hasil_lat", selectedLat)
            resultIntent.putExtra("hasil_lng", selectedLng)
            resultIntent.putExtra("hasil_alamat", selectedAddress)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latAwal = intent.getDoubleExtra("awal_lat", 0.0)
        val lngAwal = intent.getDoubleExtra("awal_lng", 0.0)
        if (latAwal != 0.0) {
            val startPos = LatLng(latAwal, lngAwal)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 15f))
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(selectedLat, selectedLng), 10f))
        }

        mMap.setOnCameraIdleListener {
            val center = mMap.cameraPosition.target
            selectedLat = center.latitude
            selectedLng = center.longitude

            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(selectedLat, selectedLng, 1)
                if (!addresses.isNullOrEmpty()) {
                    selectedAddress = addresses[0].getAddressLine(0)
                    btnConfirm.text = "Pilih Lokasi Ini"
                }
            } catch (e: Exception) {
                selectedAddress = "Lokasi Terpilih"
            }
        }
    }
}