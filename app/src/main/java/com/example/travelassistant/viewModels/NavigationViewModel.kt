package com.example.travelassistant.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationViewModel : ViewModel() {
    val showCurrentUser = MutableLiveData(false)
}