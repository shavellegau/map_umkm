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

    // Koordinat Default (Misal: Monas / Jakarta Pusat)
    private var currentLat: Double = -6.175392
    private var currentLng: Double = 106.827153
    private var currentAddress: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inisialisasi Google Places SDK jika belum (Wajib isi API Key di AndroidManifest/String)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }
        return inflater.inflate(R.layout.fragment_pilih_lokasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Sembunyikan Bottom Navigation Bar agar layar penuh
        requireActivity().findViewById<View>(R.id.bottom_nav)?.visibility = View.GONE

        // 2. Binding Views
        btnSimpan = view.findViewById(R.id.btnSimpanLokasi)
        tvAlamat = view.findViewById(R.id.tvAlamatTerdeteksi)
        centerMarker = view.findViewById(R.id.iv_center_marker) // Ikon pin merah
        toolbar = view.findViewById(R.id.toolbar)

        // 3. Setup Toolbar Back Action
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // 4. Setup Google Map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 5. Setup Autocomplete (Pencarian Lokasi)
        setupAutocomplete()

        // 6. Logika Tombol Simpan
        btnSimpan.setOnClickListener {
            handleSimpanLokasi()
        }
    }

    private fun setupAutocomplete() {
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocompleteFragment)
                as? AutocompleteSupportFragment

        // Tentukan data apa yang ingin diambil (ID, Nama, Koordinat)
        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Saat user memilih lokasi dari search bar, pindahkan kamera map ke sana
                place.latLng?.let { latLng ->
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                    // Listener onCameraIdle nanti akan otomatis mengupdate alamat teks
                }
            }

            override fun onError(status: Status) {
                Log.e("PilihLokasi", "Error pencarian: $status")
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Setting UI Map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true

        // Pindahkan kamera ke posisi awal
        val startLocation = LatLng(currentLat, currentLng)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))

        // Listener saat Map berhenti digeser (Idle)
        map.setOnCameraIdleListener {
            val center = map.cameraPosition.target
            currentLat = center.latitude
            currentLng = center.longitude

            // Animasi Marker (opsional, biar ada efek 'lompat' dikit saat geser)
            centerMarker.animate().translationY(-10f).setDuration(100).withEndAction {
                centerMarker.animate().translationY(0f).setDuration(100).start()
            }.start()

            // Ambil nama jalan dari koordinat (Reverse Geocoding)
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
                "hasil_lng" to currentLng
            )

            // Cek apakah mode "Tambah Baru" atau "Edit/Pilih Ulang"
            val isCreateNew = arguments?.getBoolean("mode_create_new") ?: false

            if (isCreateNew) {
                // FLOW A: Kirim hasil ke Form Tambah Alamat
                // Kita kirim result via setFragmentResult (agar didengar Fragment tujuan)
                // DAN kirim bundle via arguments navigation (agar aman)
                setFragmentResult("requestKey_lokasi", dataBundle)

                // Pastikan ID action ini ada di nav_graph.xml
                findNavController().navigate(R.id.action_pilihLokasiFragment_to_addEditAddressFragment, dataBundle)
            } else {
                // FLOW B: Hanya kembali ke layar sebelumnya (misal dipanggil dari layar Edit)
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
        // Kembalikan Bottom Nav saat keluar dari fragment ini
        requireActivity().findViewById<View>(R.id.bottom_nav)?.visibility = View.VISIBLE
    }
}