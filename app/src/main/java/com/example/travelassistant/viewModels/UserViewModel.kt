package com.example.travelassistant.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.image.ImageURL
import com.example.travelassistant.models.user.TodoItem
import com.example.travelassistant.models.user.User
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.openai.TravelAssistant
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user


    fun getUser(userId: String) {
        viewModelScope.launch {
            userRepository.getById(userId).collect { userData ->
                if (userData == null) {
                    return@collect
                }
                _user.postValue(userData!!)
                TravelAssistant.setUser(userData)
            }
        }
    }


    fun onboard(
        id: String,
        displayName: String,
        email: String,
        imageURL: Uri,
        currentLocation: GeoPoint,
        keepLocationPrivate: Boolean
    ) {
        viewModelScope.launch {
            userRepository.onboard(
                id,
                displayName,
                email,
                imageURL,
                currentLocation,
                keepLocationPrivate = keepLocationPrivate
            )
            getUser(id)
        }
    }

    fun updateSettings(id: String, location: GeoPoint, keepLocationPrivate: Boolean) {
        viewModelScope.launch {
            userRepository.updateSettings(id, location, keepLocationPrivate )
            _user.value = _user.value?.copy(currentLocation = location, keepLocationPrivate = keepLocationPrivate)
        }
    }

    fun updatePoints(id: String, points: Int) {
        viewModelScope.launch {
            userRepository.updatePoints(id, points)
            _user.value = _user.value?.copy(points = points)
        }
    }

    fun addTodoItem(userId: String, todoItem: TodoItem) {
        viewModelScope.launch {
            userRepository.addTodoItem(userId, todoItem)
            _user.value?.todoList?.let { _user.value = _user.value?.copy(todoList = it + todoItem) }
        }
    }

    fun deleteTodoItem(userId: String, todoId: String) {
        viewModelScope.launch {
            userRepository.deleteTodoItem(userId, todoId)
            _user.value?.todoList?.let {
                _user.value =
                    _user.value?.copy(todoList = it.filterNot { item -> item.task == todoId })
            }
        }
    }

    fun decreasePromptCount(id: String) {
        viewModelScope.launch {
            var curr = _user.value?.promptCount ?: 0
            curr = if (curr == 0) 0 else curr - 1

            userRepository.decreasePromptCount(id, curr)
            _user.value = _user.value?.copy(promptCount = curr)
        }
    }
}


class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) return UserViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}