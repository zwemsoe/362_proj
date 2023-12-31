package com.example.travelassistant.ui.settings


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
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.BuildConfig
import com.example.travelassistant.R
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.openai.TravelAssistant
import com.example.travelassistant.ui.home.HomeViewModel
import com.example.travelassistant.ui.todo.TodoViewModel
import com.example.travelassistant.utils.CommonUtil.extractDisplayName
import com.example.travelassistant.utils.CoordinatesUtil
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    private lateinit var editLocationButton: ImageButton
    private lateinit var placesClient: PlacesClient
    private lateinit var auth: FirebaseAuth
    private var marker: Marker? = null
    private var useLiveLocation = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        view = inflater.inflate(R.layout.fragment_settings, container, false)
        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.GOOGLE_MAPS_API_KEY)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars()
        setupMap()

        saveSettingsButton.setOnClickListener {
            val userId = auth.currentUser!!.uid
            val email = auth.currentUser!!.email
            val displayName = onboardingViewModel.displayName.value
            val currentLocation =
                onboardingViewModel.locationLatLng.value ?: return@setOnClickListener

            val location = GeoPoint(currentLocation.latitude, currentLocation.longitude)
            val keepLocationPrivate = keepPrivateCheckBox.isChecked
            if (userViewModel.user.value == null) {
                if (!displayName.isNullOrEmpty() && auth.currentUser!!.photoUrl != null) {
                    userViewModel.onboard(
                        userId,
                        displayName,
                        email!!,
                        auth.currentUser!!.photoUrl!!,
                        location,
                        keepLocationPrivate
                    )
                }
                navigateToHome()
            } else {
                userViewModel.updateSettings(
                    userId,
                    location,
                    keepLocationPrivate
                )
                refreshSuggestions(location)
                Toast.makeText(requireContext(), "Saved changes.", Toast.LENGTH_SHORT).show()
            }

        }

        userViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                nameTextView.text = "Hi, ${extractDisplayName(it.displayName)}!"
                keepPrivateCheckBox.isChecked = it.keepLocationPrivate
                setCurrentLocation(
                    GeoPoint(
                        it.currentLocation!!.latitude,
                        it.currentLocation!!.longitude
                    )
                )
            }
        }

        userLocationTextView.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            onAutoCompleteItemClick(selected)
        }
        userLocationTextView.doAfterTextChanged {
            if (it.toString().length < 2) {
                // Save some API calls, wait for longer entry
                return@doAfterTextChanged
            }
            findAutocompletePredictions(it.toString())
        }

        editLocationButton.setOnClickListener {
            useLiveLocation = !useLiveLocation
            onEditLocationButtonClicked()
        }

        onboardingViewModel.address.observe(viewLifecycleOwner) {
            if (it != null) {
                userLocationTextView.setText(it)
            }
        }

        onboardingViewModel.displayName.observe(viewLifecycleOwner) {
            nameTextView.text = "Hi, ${extractDisplayName(it)}!"
        }

        if (userViewModel.user.value == null) {
            onboardingViewModel.fetchLastLocation(requireActivity(), locationProviderClient)
        }
    }

    private fun refreshSuggestions(geoPoint: GeoPoint) {
        TravelAssistant.setUserLocation(geoPoint)
        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        homeViewModel.generateSuggestions()
        val todoViewModel = ViewModelProvider(requireActivity())[TodoViewModel::class.java]
        todoViewModel.generateSuggestions()
    }

    private fun setCurrentLocation(currentLocation: GeoPoint) {
        lifecycleScope.launch {
            val addressList =
                CoordinatesUtil.getAddressFromLocation(requireContext(), currentLocation)
            withContext(Dispatchers.Main) {
                if (addressList.isNotEmpty()) {
                    userLocationTextView.setText(addressList[0].getAddressLine(0))
                }
            }
        }
    }

    private fun onEditLocationButtonClicked() {
        if (useLiveLocation) {
            editLocationButton.setImageResource(R.drawable.ic_edit)
            userLocationTextView.isEnabled = false
            onboardingViewModel.fetchLastLocation(requireActivity(), locationProviderClient)
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
        onboardingViewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        nameTextView = view.findViewById(R.id.display_name_settings)
        saveSettingsButton = view.findViewById(R.id.save_settings_button)
        initEditLocationAutoComplete()
        keepPrivateCheckBox = view.findViewById(R.id.keep_private)
        editLocationButton = view.findViewById(R.id.edit_my_location)

        userRepository = UserRepository()
        userViewModel = ViewModelProvider(
            requireActivity(), UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]
        placesClient = Places.createClient(requireContext())
    }

    private fun onAutoCompleteItemClick(itemSelected: String) {
        onboardingViewModel.setLocationFromAddress(requireContext(), itemSelected)
    }

    /**
     * https://developers.google.com/maps/documentation/places/android-sdk/autocomplete
     */
    private fun findAutocompletePredictions(query: String) {
        fun buildRequest(token: AutocompleteSessionToken): FindAutocompletePredictionsRequest {
            return FindAutocompletePredictionsRequest.builder().setSessionToken(token)
                .setQuery(query).build()
        }

        fun onSuccess(response: FindAutocompletePredictionsResponse) {
            val locations = mutableListOf<String>()
            for (prediction in response.autocompletePredictions) {
                locations.add(prediction.getFullText(null).toString())
            }
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, locations
            )
            userLocationTextView.threshold = 2
            userLocationTextView.setAdapter(adapter)
        }

        fun onFailure(exception: Exception?) {
            if (exception is ApiException) {
                println("Place not found: ${exception.statusCode}")
            }
        }

        val token = AutocompleteSessionToken.newInstance()
        val request = buildRequest(token)
        placesClient.findAutocompletePredictions(request).addOnSuccessListener(::onSuccess)
            .addOnFailureListener(::onFailure)
    }

    private fun setupMap() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        onboardingViewModel.locationLatLng.observe(viewLifecycleOwner) {
            val latLng = LatLng(it.latitude, it.longitude)
            showMarker(latLng)
        }

        userViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                val latLng = LatLng(it.currentLocation!!.latitude, it.currentLocation!!.longitude)
                showMarker(latLng)
            }
        }
    }

    private fun showMarker(latLng: LatLng) {
        val markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        marker?.remove()
        marker = googleMap.addMarker(
            MarkerOptions().position(latLng).title("You").icon(markerIcon)
        )
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        googleMap.animateCamera(cameraUpdate)
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_nav_settings_to_nav_home)
    }
}
