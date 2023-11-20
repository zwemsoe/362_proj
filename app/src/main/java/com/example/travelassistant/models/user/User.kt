package com.example.travelassistant.models.user
import com.google.firebase.firestore.GeoPoint

data class TodoItem(
    val id: String,
    val task: String,
    val completed: Boolean
)

data class User(
    val id: String,
    val displayName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val currentLocation: GeoPoint,
    val points: Int = 0,
    val todoList: List<TodoItem> = listOf()
)
