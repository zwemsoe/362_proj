package com.example.travelassistant.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.R
import com.example.travelassistant.viewModels.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class SettingsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var view: View
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var saveSettingsButton: Button
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var userLocationTextView: TextView

    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_settings, container, false)
        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveSettingsButton = view.findViewById(R.id.save_settings_button)

        saveSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_nav_settings_to_nav_home)
        }

        userLocationTextView = view.findViewById(R.id.user_address)

        setupMap()

        locationViewModel.address.observe(viewLifecycleOwner) {
            if(it != null){
                userLocationTextView.text = it.getAddressLine(0)
            }
        }

        locationViewModel.fetchLastLocation(requireActivity(), locationProviderClient)
    }

    private fun setupMap() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        locationViewModel.locationLatLng.observe(viewLifecycleOwner) {
            val latLng = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(
                MarkerOptions().position(latLng).title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            googleMap.animateCamera(cameraUpdate)

        }
    }
}
