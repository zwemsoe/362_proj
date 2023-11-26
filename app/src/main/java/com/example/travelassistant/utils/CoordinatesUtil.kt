package com.example.travelassistant.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.firebase.firestore.GeoPoint
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object CoordinatesUtil {
    suspend fun getAddressFromLocation(
        context: Context, geoPoint: GeoPoint, maxResults: Int = 1
    ): List<Address> = suspendCoroutine { continuation ->
        getAddressFromLocation(context, geoPoint.latitude, geoPoint.longitude, { addresses ->
            continuation.resume(addresses)
        }, maxResults)
    }

    fun getAddressFromLocation(
        context: Context, location: Location, callback: (List<Address>) -> Unit, maxResults: Int = 1
    ) {
        getAddressFromLocation(context, location.latitude, location.longitude, callback, maxResults)
    }

    @Suppress("unused")
    fun getAddressFromLocation(
        context: Context, geoPoint: GeoPoint, callback: (List<Address>) -> Unit, maxResults: Int = 1
    ) {
        getAddressFromLocation(context, geoPoint.latitude, geoPoint.longitude, callback, maxResults)
    }

    @Suppress("DEPRECATION")
    fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        callback: (List<Address>) -> Unit,
        maxResults: Int = 1
    ) {
        val geocoder = Geocoder(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, maxResults, callback)
        } else {
            val addresses = geocoder.getFromLocation(latitude, longitude, maxResults) ?: return
            callback(addresses)
        }
    }

    @Suppress("DEPRECATION")
    fun getAddressFromLocationName(
        context: Context, address: String, callback: (List<Address>) -> Unit, maxResults: Int = 1
    ) {
        val geocoder = Geocoder(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(address, maxResults, callback)
        } else {
            val addresses = geocoder.getFromLocationName(address, maxResults) ?: return
            callback(addresses)
        }
    }
}