package com.example.map_umkm

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class PilihLokasiFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var btnSimpan: Button
    private lateinit var tvAlamat: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var centerMarker: ImageView

    private var currentLat: Double = -6.175392
    private var currentLng: Double = 106.827153
    private var currentAddress: String = ""
    private var fromPayment: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }
        return inflater.inflate(R.layout.fragment_pilih_lokasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fromPayment = arguments?.getBoolean("from_payment") ?: false

        requireActivity().findViewById<View>(R.id.bottom_nav)?.visibility = View.GONE

        btnSimpan = view.findViewById(R.id.btnSimpanLokasi)
        tvAlamat = view.findViewById(R.id.tvAlamatTerdeteksi)
        centerMarker = view.findViewById(R.id.iv_center_marker)
        toolbar = view.findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupAutocomplete()

        btnSimpan.setOnClickListener {
            handleSimpanLokasi()
        }
    }

    private fun setupAutocomplete() {
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocompleteFragment) as? AutocompleteSupportFragment
        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let { latLng ->
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
            }

            override fun onError(status: Status) {
                Log.e("PilihLokasi", "Error pencarian: $status")
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true

        val startLocation = LatLng(currentLat, currentLng)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))

        map.setOnCameraIdleListener {
            val center = map.cameraPosition.target
            currentLat = center.latitude
            currentLng = center.longitude

            centerMarker.animate().translationY(-10f).setDuration(100).withEndAction {
                centerMarker.animate().translationY(0f).setDuration(100).start()
            }.start()

            val addressName = getAddressName(currentLat, currentLng)
            currentAddress = addressName
            tvAlamat.text = addressName
            btnSimpan.isEnabled = true
        }

        map.setOnCameraMoveStartedListener {
            tvAlamat.text = "Mengambil lokasi..."
            btnSimpan.isEnabled = false
        }
    }

    private fun handleSimpanLokasi() {
        if (currentAddress.isNotEmpty()) {
            val dataBundle = bundleOf(
                "hasil_alamat" to currentAddress,
                "hasil_lat" to currentLat,
                "hasil_lng" to currentLng,
                "from_payment" to fromPayment // <-- BUG FIX: Pass the flag along
            )

            if (fromPayment) {
                // Jika datang dari PaymentFragment, navigasi ke AddEditAddressFragment
                findNavController().navigate(R.id.action_pilihLokasiFragment_to_addEditAddressFragment, dataBundle)
            } else {
                // Jika dari tempat lain (misal edit alamat), kembali ke fragment sebelumnya
                setFragmentResult("requestKey_lokasi", dataBundle)
                findNavController().popBackStack()
            }
        } else {
            Toast.makeText(requireContext(), "Mohon tunggu, alamat sedang dimuat...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressName(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)
            } else {
                "Alamat tidak ditemukan"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Gagal memuat nama jalan"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_nav)?.visibility = View.VISIBLE
    }
}
