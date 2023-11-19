package com.example.travelassistant.ui.onboarding

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.R
import com.example.travelassistant.utils.PermissionUtil

class RequestPermissionsFragment : Fragment() {
    private lateinit var view: View
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_request_permissions, container, false)

        view.findViewById<Button>(R.id.button_request_permissions).setOnClickListener {
            handlePermissionRequests()
        }
        return view
    }

    private fun onPermissionsGranted() {
        findNavController().navigate(R.id.action_requestPermissionsFragment_to_nav_home)
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
}