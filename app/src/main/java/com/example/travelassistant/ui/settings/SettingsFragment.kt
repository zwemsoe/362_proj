package com.example.travelassistant.ui.settings

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.BuildConfig
import com.example.travelassistant.R
import com.example.travelassistant.manager.DataStoreManager
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.viewModels.OnboardingViewModel
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
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
    private lateinit var userLocationTextView: AutoCompleteTextView
    private lateinit var nameTextView: TextView
    private lateinit var keepPrivateCheckBox: CheckBox
    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var editLocationButton: ImageButton

    private var useLiveLocation = true

    //    private lateinit var autoCompleteFragment: AutocompleteSupportFragment
    private lateinit var placesClient: PlacesClient

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataStoreManager = DataStoreManager(context)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_settings, container, false)
        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.GOOGLE_MAPS_API_KEY);
        }

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

        userLocationTextView.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            onAutoCompleteItemClick(selected)
        }
        userLocationTextView.doAfterTextChanged {
            findAutocompletePredictions(it.toString())
        }

        editLocationButton.setOnClickListener {
            useLiveLocation = !useLiveLocation
            onEditLocationButtonClicked()
        }

        onboardingViewModel.address.observe(viewLifecycleOwner) {
            if (it != null && useLiveLocation) {
                userLocationTextView.setText(it)
            }
        }

        onboardingViewModel.displayName.observe(viewLifecycleOwner) {
            nameTextView.text = "Hi, $it!"
        }

        onboardingViewModel.fetchLastLocation(requireActivity(), locationProviderClient)
    }

    private fun onEditLocationButtonClicked() {
        if (useLiveLocation) {
            editLocationButton.setImageResource(R.drawable.ic_edit)
            userLocationTextView.isEnabled = false
            onboardingViewModel.refreshLocation()
        } else {
            editLocationButton.setImageResource(R.drawable.ic_my_location)
            userLocationTextView.isEnabled = true
        }
    }

    private fun initEditLocationAutoComplete() {
        userLocationTextView = view.findViewById(R.id.user_address)
        userLocationTextView.isEnabled = false
    }

    private fun initVars() {
        onboardingViewModel =
            ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)
        nameTextView = view.findViewById(R.id.display_name_settings)
        saveSettingsButton = view.findViewById(R.id.save_settings_button)
        initEditLocationAutoComplete()
        keepPrivateCheckBox = view.findViewById(R.id.keep_private)
        editLocationButton = view.findViewById(R.id.edit_my_location)
//        autoCompleteFragment =
//            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        userRepository = UserRepository()
        userViewModel = ViewModelProvider(
            requireActivity(), UserViewModelFactory(userRepository)
        ).get(UserViewModel::class.java)
        placesClient = Places.createClient(requireContext())
    }

    private fun storeUserId(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreManager.storeUserId(id)
        }
    }

    private fun onAutoCompleteItemClick(itemSelected: String) {
        fun handleSelectedLocation(addresses: List<Address>) {
            if (addresses.isNotEmpty()) {
                val location = addresses[0]
                val latitude = location.latitude
                val longitude = location.longitude
                // TODO
            }
        }

        val geocoder = Geocoder(requireContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName("address", 1, ::handleSelectedLocation)
        } else {
            val addresses = geocoder.getFromLocationName(itemSelected, 1) ?: return
            handleSelectedLocation(addresses)
        }
    }

    /**
     * https://developers.google.com/maps/documentation/places/android-sdk/autocomplete
     */
    private fun findAutocompletePredictions(query: String) {
        fun buildRequest(token: AutocompleteSessionToken): FindAutocompletePredictionsRequest {
            return FindAutocompletePredictionsRequest
                .builder()
//                .setTypesFilter(listOf(PlaceTypes.ADDRESS))
                .setSessionToken(token).setQuery(query).build()
        }

        fun onSuccess(response: FindAutocompletePredictionsResponse) {
            val locations = mutableListOf<String>()
            for (prediction in response.autocompletePredictions) {
                println(prediction.placeId)
                println(prediction.getFullText(null).toString())
                locations.add(prediction.getFullText(null).toString())
            }
            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    locations
                )
            userLocationTextView.threshold = 2
            userLocationTextView.setAdapter(adapter)
        }

        fun onFailure(exception: Exception?) {
            if (exception is ApiException) {
                println("Place not found: ${exception.statusCode}")
            }
        }

        println(query)
        val token = AutocompleteSessionToken.newInstance()
        val request = buildRequest(token)
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener(::onSuccess)
            .addOnFailureListener(::onFailure)
    }

    private fun setupMap() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        val markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        onboardingViewModel.locationLatLng.observe(viewLifecycleOwner) {
            val latLng = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(
                MarkerOptions().position(latLng).title("You")
                    .icon(markerIcon)
            )
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            googleMap.animateCamera(cameraUpdate)

        }
    }
}
