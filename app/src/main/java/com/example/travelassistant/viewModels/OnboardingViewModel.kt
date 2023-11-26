package com.example.travelassistant.viewModels

import android.app.Activity
import android.content.Context
import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelassistant.utils.CoordinatesUtil.getAddressFromLocation
import com.example.travelassistant.utils.CoordinatesUtil.getAddressFromLocationName
import com.example.travelassistant.utils.PermissionUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OnboardingViewModel : ViewModel() {
    private val _locationLatLng = MutableLiveData<LatLng>()
    val locationLatLng: LiveData<LatLng> = _locationLatLng

    private val _address = MutableLiveData<String?>()
    val address: LiveData<String?> = _address

    private val _displayName = MutableLiveData<String>()
    val displayName: LiveData<String> = _displayName


    fun updateDisplayName(name: String) {
        _displayName.postValue(name)
    }

    fun fetchLastLocation(activity: Activity, locationProviderClient: FusedLocationProviderClient) {
        viewModelScope.launch {
            try {
                PermissionUtil.checkLocationPermission(activity)
                val location = locationProviderClient.lastLocation.await()

                getAddressFromLocation(activity, location, { addressList ->
                    if (addressList.isNotEmpty()) {
                        _address.postValue(addressList[0].getAddressLine(0))
                    }
                })
                _locationLatLng.postValue(LatLng(location.latitude, location.longitude))
            } catch (e: Exception) {
                println("Error with checking/requesting permission")
            }
        }
    }

    fun setLocationFromAddress(context: Context, address: String) {
        viewModelScope.launch {
            fun handleSelectedLocation(addresses: List<Address>) {
                if (addresses.isNotEmpty()) {
                    val location = addresses[0]
                    _address.postValue(address)
                    _locationLatLng.postValue(LatLng(location.latitude, location.longitude))
                }
            }

            getAddressFromLocationName(context, address, ::handleSelectedLocation)
        }
    }
}
