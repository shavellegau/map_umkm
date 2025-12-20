package com.example.map_umkm

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class PilihLokasiFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var btnSimpan: Button
    private lateinit var tvAlamat: TextView
    private lateinit var toolbar: Toolbar

    private var currentLat: Double = -6.175392
    private var currentLng: Double = 106.827153
    private var currentAddress: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pilih_lokasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_nav)?.visibility = View.GONE

        btnSimpan = view.findViewById(R.id.btnSimpanLokasi)
        tvAlamat = view.findViewById(R.id.tvAlamatTerdeteksi)
        toolbar = view.findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSimpan.setOnClickListener {
            if (currentAddress.isNotEmpty()) {
                val dataBundle = bundleOf(
                    "hasil_alamat" to currentAddress,
                    "hasil_lat" to currentLat,
                    "hasil_lng" to currentLng
                )

                val isFromPayment = arguments?.getBoolean("from_payment") ?: false

                if (isFromPayment) {
                    // PERBAIKAN: Jika dari payment, kembalikan hasil LANGSUNG ke PaymentFragment
                    // Menggunakan key "request_manual_map" yang sudah disiapkan di PaymentFragment
                    setFragmentResult("request_manual_map", dataBundle)
                    findNavController().popBackStack()
                } else {
                    // Jika dari form tambah alamat (edit address), kembalikan ke form tersebut
                    setFragmentResult("requestKey_lokasi", dataBundle)
                    findNavController().popBackStack()
                }
            } else {
                Toast.makeText(requireContext(), "Sedang memuat alamat...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val startLocation = LatLng(currentLat, currentLng)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))

        map.setOnCameraIdleListener {
            val center = map.cameraPosition.target
            currentLat = center.latitude
            currentLng = center.longitude

            val addressName = getAddressName(currentLat, currentLng)
            currentAddress = addressName
            tvAlamat.text = addressName
        }
    }

    private fun getAddressName(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.get(0)?.getAddressLine(0) ?: "Alamat tidak ditemukan"
        } catch (e: Exception) { "Gagal memuat alamat" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_nav)?.visibility = View.VISIBLE
    }
}