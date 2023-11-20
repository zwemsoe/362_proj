package com.example.travelassistant.models.user

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Queries
    fun getById(userId: String): Flow<User?> = flow {
        val snapshot = db.collection("users").document(userId).get().await()
        val user = snapshot.toObject(User::class.java)
        emit(user)
    }

    // Mutations
    suspend fun onboard(
        id: String,
        displayName: String,
        currentLocation: GeoPoint,
        keepLocationPrivate: Boolean
    ) {
        val user = User(
            id = id,
            displayName = displayName,
            currentLocation = currentLocation,
            keepLocationPrivate = keepLocationPrivate
        )
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun updateName(userId: String, name: String) {
        usersCollection.document(userId).update("displayName", name).await()
    }

    suspend fun updatePoints(userId: String, newPoints: Int) {
        usersCollection.document(userId).update("points", newPoints).await()
    }

    suspend fun addTodoItem(userId: String, todoItem: TodoItem) {
        val doc = usersCollection.document(userId)
        db.runTransaction { transaction ->
            val user = transaction.get(doc).toObject(User::class.java)
            val updatedTodoList = user?.todoList?.plus(todoItem) ?: listOf(todoItem)
            transaction.update(doc, "todoList", updatedTodoList)
        }.await()
    }

    suspend fun deleteTodoItem(userId: String, todoId: String) {
        val doc = usersCollection.document(userId)
        db.runTransaction { transaction ->
            val user = transaction.get(doc).toObject(User::class.java)
            val updatedTodoList = user?.todoList?.filterNot { it.task == todoId } ?: listOf()
            transaction.update(doc, "todoList", updatedTodoList)
        }.await()
    }
}