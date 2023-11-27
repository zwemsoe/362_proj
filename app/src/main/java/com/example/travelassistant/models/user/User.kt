package com.example.travelassistant.models.user

import com.google.firebase.firestore.GeoPoint

data class TodoItem(
    val id: String, val task: String, val completed: Boolean
)

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val currentLocation: GeoPoint?,
    val createdAt: Long = System.currentTimeMillis(),
    val keepLocationPrivate: Boolean = false,
    val points: Int = 0,
    val todoList: List<TodoItem> = listOf()
) {
    // https://medium.com/@eugenebrusov/firebase-needs-an-empty-constructor-to-be-able-to-deserialize-the-objects-2ddbd2c03620
    constructor() : this(
        id = "",
        displayName = "",
        email = "",
        currentLocation = null,
    )
}
