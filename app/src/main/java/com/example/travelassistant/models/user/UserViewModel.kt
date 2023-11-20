package com.example.travelassistant.models.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    fun getUser(userId: String) {
        viewModelScope.launch {
            userRepository.getById(userId).collect { userData ->
                _user.value = userData
            }
        }
    }

    fun onboard(id: String, displayName: String, currentLocation: GeoPoint) {
        viewModelScope.launch {
            userRepository.onboard(id, displayName, currentLocation)
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
            _user.value?.todoList?.let { _user.value = _user.value?.copy(todoList = it.filterNot { item -> item.task == todoId }) }
        }
    }
}
