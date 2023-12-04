package com.example.travelassistant.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class NavigationViewModel : ViewModel() {
    val showCurrentUser = MutableLiveData<Boolean>(false)
}