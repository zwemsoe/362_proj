package com.example.travelassistant.viewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelassistant.models.user.User
import com.example.travelassistant.models.user.UserRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user


    fun getUser(userId: String) {
        viewModelScope.launch {
            userRepository.getById(userId).collect { userData ->
                _user.postValue(userData)
            }
        }
    }
}


class ProfileViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) return ProfileViewModel(
            repository
        ) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}