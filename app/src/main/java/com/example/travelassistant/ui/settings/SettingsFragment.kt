package com.example.travelassistant.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.DataStoreManager
import com.example.travelassistant.R
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.viewModels.OnboardingViewModel
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID


class SettingsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var view: View
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var saveSettingsButton: Button
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var userLocationTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var keepPrivateCheckBox: CheckBox
    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var dataStoreManager: DataStoreManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataStoreManager = DataStoreManager(context)
    }


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
        initVars()
        setupMap()

        saveSettingsButton.setOnClickListener {
            val userId = UUID.randomUUID().toString()
            val displayName = onboardingViewModel.displayName.value
            val currentLocation = onboardingViewModel.locationLatLng.value
            val location = GeoPoint(currentLocation!!.latitude, currentLocation.longitude)
            val keepLocationPrivate = keepPrivateCheckBox.isChecked
            if (!displayName.isNullOrEmpty() && location != null) {
                userViewModel.onboard(userId, displayName, location, keepLocationPrivate)
                storeUserId(userId)
            }
            findNavController().navigate(R.id.action_nav_settings_to_nav_home)
        }

        onboardingViewModel.address.observe(viewLifecycleOwner) {
            if (it != null) {
                userLocationTextView.text = it
            }
        }

        onboardingViewModel.displayName.observe(viewLifecycleOwner) {
            nameTextView.text = "Hi, $it!"
        }

        onboardingViewModel.fetchLastLocation(requireActivity(), locationProviderClient)
    }

    private fun initVars() {
        onboardingViewModel =
            ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        nameTextView = view.findViewById(R.id.display_name_settings)
        saveSettingsButton = view.findViewById(R.id.save_settings_button)
        userLocationTextView = view.findViewById(R.id.user_address)
        keepPrivateCheckBox = view.findViewById(R.id.keep_private)

        userRepository = UserRepository()
        userViewModel = ViewModelProvider(
            requireActivity(),
            UserViewModelFactory(userRepository)
        ).get(UserViewModel::class.java)
    }

    private fun storeUserId(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.storeUserId(id)
        }
    }



    private fun setupMap() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        onboardingViewModel.locationLatLng.observe(viewLifecycleOwner) {
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
