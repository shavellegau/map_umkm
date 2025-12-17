package com.example.map_umkm

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Address as GeocoderAddress
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.map_umkm.databinding.FragmentPilihLokasiBinding
import com.example.map_umkm.model.Address
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class PilihLokasiFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPilihLokasiBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private lateinit var geocoder: Geocoder

    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null
    private var currentLocationMarker: Marker? = null
    private var isMapMovementFromAutocomplete: Boolean = false

    // --- FIX UTAMA: Definisi Launcher Tanpa Referensi Diri Sendiri ---
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getLastLocation()
            } else {
                // Jika ditolak, jangan langsung panggil launcher lagi di sini.
                // Panggil fungsi terpisah untuk menampilkan dialog.
                showPermissionRationaleDialog()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPilihLokasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geocoder = Geocoder(requireContext(), Locale.getDefault())

        val mapFragment = childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupAutocompleteFragment()

        binding.btnSimpanLokasi.setOnClickListener {
            val currentLatLng = selectedLatLng
            val currentAddr = selectedAddress

            if (currentLatLng != null && currentAddr != null) {
                val newAddress = Address(
                    recipientName = "",
                    phoneNumber = "",
                    fullAddress = currentAddr,
                    latitude = currentLatLng.latitude,
                    longitude = currentLatLng.longitude,
                    isPrimary = false
                )
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedLocation", newAddress)
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Lokasi terpilih tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMapListeners()

        checkAndRequestPermission()
    }

    // --- Fungsi Helper untuk Izin ---

    private fun checkAndRequestPermission() {
        when {
            hasLocationPermission() -> {
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                // Panggil launcher dari sini aman karena launcher sudah dibuat sebelumnya
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Izin Lokasi")
            .setMessage("Aplikasi ini memerlukan akses lokasi Anda untuk memilih lokasi pengiriman yang akurat.")
            .setPositiveButton("Izinkan") { _, _ ->
                // Panggil launcher di sini aman
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
                setDefaultLocation()
            }
            .create().show()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    @Throws(SecurityException::class)
    private fun getLastLocation() {
        if (hasLocationPermission()) {
            googleMap?.isMyLocationEnabled = true
            try {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val userLocation = LatLng(location.latitude, location.longitude)
                            updateMapLocation(userLocation)
                            addMarkerAtLocation(userLocation, "Lokasi Anda")
                            reverseGeocodeAndUpdateUI(userLocation)
                        } else {
                            setDefaultLocation()
                            Toast.makeText(requireContext(), "Tidak dapat mendeteksi lokasi terakhir.", Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${e.message}")
            }
        }
    }

    private fun setDefaultLocation() {
        // Monas sebagai default jika izin ditolak atau lokasi null
        val defaultLoc = LatLng(-6.175392, 106.827153)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 12f))
    }

    private fun updateMapLocation(location: LatLng) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
    }

    private fun addMarkerAtLocation(location: LatLng, title: String) {
        currentLocationMarker?.remove()
        currentLocationMarker = googleMap?.addMarker(MarkerOptions().title(title).position(location))
    }

    private fun reverseGeocodeAndUpdateUI(latLng: LatLng) {
        lifecycleScope.launch(Dispatchers.Main) {
            val resultText: String = withContext(Dispatchers.IO) {
                var addrString = "Alamat tidak ditemukan"
                try {
                    val addresses: List<GeocoderAddress>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val firstAddress: GeocoderAddress = addresses[0]
                        val line: String? = firstAddress.getAddressLine(0)
                        if (line != null) {
                            addrString = line
                        }
                    }
                } catch (e: IOException) {
                    Log.e("Geocoder", "Error network", e)
                    addrString = "Layanan alamat tidak tersedia"
                } catch (e: Exception) {
                    Log.e("Geocoder", "Error lain", e)
                }
                addrString
            }
            updateUI(latLng, resultText)
        }
    }

    private fun setupAutocompleteFragment() {
        if (!Places.isInitialized()) {
            try {
                val appInfo = requireContext().packageManager.getApplicationInfo(
                    requireContext().packageName,
                    PackageManager.GET_META_DATA
                )
                val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY")

                if (!apiKey.isNullOrEmpty()) {
                    Places.initialize(requireContext(), apiKey)
                }
            } catch (e: Exception) {
                Log.e("Places", "Gagal init places")
            }
        }

        val autocompleteFragment =
            childFragmentManager.findFragmentById(binding.autocompleteFragment.id) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                isMapMovementFromAutocomplete = true
                val location = place.latLng
                val address = place.address

                if (location != null) {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                    updateUI(location, address)
                }
            }

            override fun onError(status: Status) {
                Log.e("Places", "Error: $status")
                updateUI(null, null)
            }
        })
    }

    private fun setupMapListeners() {
        googleMap?.setOnCameraIdleListener {
            if (!isMapMovementFromAutocomplete) {
                val centerMap = googleMap?.cameraPosition?.target
                if (centerMap != null) {
                    reverseGeocodeAndUpdateUI(centerMap)
                }
            }
            isMapMovementFromAutocomplete = false
        }
    }

    private fun updateUI(location: LatLng?, address: String?) {
        selectedLatLng = location
        selectedAddress = address

        val safeAddress = address ?: ""
        val isValid = safeAddress.isNotEmpty() &&
                !safeAddress.contains("tidak ditemukan", ignoreCase = true) &&
                !safeAddress.contains("tidak tersedia", ignoreCase = true)

        if (isValid) {
            binding.tvAlamatTerdeteksi.text = safeAddress
            binding.btnSimpanLokasi.isEnabled = true
        } else {
            binding.tvAlamatTerdeteksi.text = if (safeAddress.isNotEmpty()) safeAddress else "Geser peta untuk mencari alamat"
            binding.btnSimpanLokasi.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}