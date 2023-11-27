package com.example.travelassistant.ui.onboarding

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.postDelayed
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.R
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.utils.PermissionUtil
import com.example.travelassistant.utils.slideUpAnimation
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.Timer
import kotlin.concurrent.schedule

private const val NAVIGATION_DELAY = 3000L

class RequestPermissionsFragment : Fragment() {
    private lateinit var view: View
    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var auth: FirebaseAuth
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )
    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.entries.all { it.value }
        if (granted) {
            onPermissionsGranted()
            return@registerForActivityResult
        }
        // Request permissions again
        requestPermissions()
    }
    private lateinit var requestPermissionsButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        view = inflater.inflate(R.layout.fragment_request_permissions, container, false)

        requestPermissionsButton = view.findViewById(R.id.button_request_permissions)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = UserRepository()
        userViewModel = ViewModelProvider(
            this@RequestPermissionsFragment, UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]

        userViewModel.getUser(auth.currentUser!!.uid)

        if (hasPermissions()) {
            onPermissionsGranted()
        } else {
            requestPermissionsButton.setOnClickListener {
                handlePermissionRequests()
            }
        }
    }

    private fun navigateToNextOnboardingScreen() {
        // TODO: Navigate away with animation
        findNavController().navigate(R.id.action_requestPermissionsFragment_to_onboardingUserInfoFragment)
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.nav_home)
    }

    private fun onPermissionsGranted() {
        // clear the listener
        requestPermissionsButton.setOnClickListener(null)
        requestPermissionsButton.text = "Thanks For Permissions!"
        val successGreen = Color.parseColor("#4BB543")
        requestPermissionsButton.setBackgroundColor(successGreen)

        //TODO: Add green checkboxes next to each permission text

        // Wait some time to show user thank you message
        Handler(Looper.getMainLooper()).postDelayed(NAVIGATION_DELAY) {
            if (userViewModel.user.value != null) {
                navigateToHome()
            } else {
                navigateToNextOnboardingScreen()
            }

        }
    }

    private fun hasPermissions(): Boolean {
        val hasLocationPermissions = PermissionUtil.hasPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) && PermissionUtil.hasPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val hasCameraPermission = PermissionUtil.hasPermission(
            requireContext(), Manifest.permission.CAMERA
        )
        return hasLocationPermissions and hasCameraPermission
    }

    private fun requestPermissions() {
        permissionsResultCallback.launch(permissions)
    }

    private fun handlePermissionRequests() {
        if (!hasPermissions()) {
            requestPermissions()
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        (activity as? AppCompatActivity)?.supportActionBar?.show()
//    }
}