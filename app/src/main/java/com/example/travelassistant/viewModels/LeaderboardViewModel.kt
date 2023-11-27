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

class LeaderboardViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users


    fun getTopUsers() {
        viewModelScope.launch {
            userRepository.getTopTenUsers().collect {
                _users.postValue(it)
            }
        }
    }
}


class LeaderboardViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) return LeaderboardViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}