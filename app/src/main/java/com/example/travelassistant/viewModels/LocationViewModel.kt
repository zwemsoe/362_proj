package com.example.travelassistant.viewModels

import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelassistant.utils.PermissionUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationViewModel : ViewModel() {
    private val _locationLatLng = MutableLiveData<LatLng>()
    val locationLatLng: LiveData<LatLng> = _locationLatLng

    private val _address = MutableLiveData<Address?>()
    val address: LiveData<Address?> = _address


    @Suppress("DEPRECATION")
    fun fetchLastLocation(activity: Activity, locationProviderClient: FusedLocationProviderClient) {
        viewModelScope.launch {
            try {
                PermissionUtil.checkLocationPermission(activity);
                val location = locationProviderClient.lastLocation.await()

                val geocoder = Geocoder(activity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()){
                    _address.postValue(addresses[0])
                }
                _locationLatLng.postValue(LatLng(location.latitude, location.longitude))
            } catch (e: Exception) {
                println("Error with checking/requesting permission")
            }
        }
    }
}