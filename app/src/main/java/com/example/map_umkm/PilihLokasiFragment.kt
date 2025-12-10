package com.example.map_umkm

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address as GeocoderAddress
import android.location.Geocoder
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
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String? = null

    private var isMapMovementFromAutocomplete = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getMyLastLocation()
            } else {
                Toast.makeText(requireContext(), "Izin lokasi diperlukan.", Toast.LENGTH_LONG).show()
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        // Setup Map
        val mapFragment = childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupAutocompleteFragment()

        // Tombol Simpan
        binding.btnSimpanLokasi.setOnClickListener {
            if (selectedLatLng != null && selectedAddress != null) {
                val partialAddress = com.example.map_umkm.model.Address(
                    fullAddress = selectedAddress!!,
                    latitude = selectedLatLng!!.latitude,
                    longitude = selectedLatLng!!.longitude
                )
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedLocation", partialAddress)
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Lokasi terpilih tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermission()
        setupMapListeners()
    }

    private fun setupAutocompleteFragment() {
        if (!Places.isInitialized()) {
            // Pastikan API Key ini valid dan memiliki akses Places API & Maps SDK
            Places.initialize(requireContext(), "AIzaSyDIrN5Cr4dSpkpWwM4dbyt7DTaPf-2PLrw")
        }

        val autocompleteFragment =
            childFragmentManager.findFragmentById(binding.autocompleteFragment.id) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                isMapMovementFromAutocomplete = true
                val location = place.latLng
                val address = place.address

                location?.let {
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 17f))
                }
                updateUI(location, address)
            }

            override fun onError(status: Status) {
                Log.e("Places", "An error occurred: $status")
                Toast.makeText(requireContext(), "Gagal mencari lokasi", Toast.LENGTH_SHORT).show()
                updateUI(null, null)
            }
        })
    }

    private fun setupMapListeners() {
        googleMap?.setOnCameraIdleListener {
            if (!isMapMovementFromAutocomplete) {
                val centerMap = googleMap?.cameraPosition?.target
                centerMap?.let {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val addresses: List<GeocoderAddress>? = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            val addressText = if (!addresses.isNullOrEmpty()) {
                                addresses[0].getAddressLine(0)
                            } else {
                                "Alamat tidak ditemukan di lokasi ini"
                            }
                            withContext(Dispatchers.Main) {
                                updateUI(it, addressText)
                            }
                        } catch (e: IOException) {
                            Log.e("Geocoder", "Layanan Geocoder tidak tersedia", e)
                            withContext(Dispatchers.Main) {
                                updateUI(it, "Layanan alamat tidak tersedia")
                            }
                        }
                    }
                }
            }
            isMapMovementFromAutocomplete = false
        }
    }

    private fun updateUI(location: LatLng?, address: String?) {
        selectedLatLng = location
        selectedAddress = address

        val isAddressValid = address != null &&
                !address.contains("tidak ditemukan", ignoreCase = true) &&
                !address.contains("tidak tersedia", ignoreCase = true)

        if (isAddressValid) {
            binding.tvAlamatTerdeteksi.text = address
            binding.btnSimpanLokasi.isEnabled = true
        } else {
            binding.tvAlamatTerdeteksi.text = address ?: "Geser peta untuk mencari alamat"
            binding.btnSimpanLokasi.isEnabled = false
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLastLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @Throws(SecurityException::class)
    private fun getMyLastLocation() {
        googleMap?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val myLatLng = LatLng(location.latitude, location.longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17f))
            } else {
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-6.2088, 106.8456), 12f))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}