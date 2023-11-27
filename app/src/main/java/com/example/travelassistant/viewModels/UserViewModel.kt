package com.example.travelassistant.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
                _user.postValue(userData)
                if (userData != null) {
                    TravelAssistant.setUser(userData)
                }
            }
        }
    }

    fun onboard(
        id: String, displayName: String, currentLocation: GeoPoint, keepLocationPrivate: Boolean
    ) {
        viewModelScope.launch {
            userRepository.onboard(
                id, displayName, currentLocation, keepLocationPrivate = keepLocationPrivate
            )
        }
    }

    fun updateName(id: String, newName: String) {
        viewModelScope.launch {
            userRepository.updateName(id, newName)
            _user.value = _user.value?.copy(displayName = newName)
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
}


class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) return UserViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}