package com.example.travelassistant.models.user

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
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

    fun getTopTenUsers(): Flow<List<User>?> = flow {
        val snapshot = db.collection("users").orderBy("points", Query.Direction.DESCENDING)
            .limit(10).get().await()
        val users = snapshot.documents.mapNotNull { document ->
            document.toObject(User::class.java)
        }
        emit(users)
    }

    // Mutations
    suspend fun onboard(
        id: String,
        displayName: String,
        email: String,
        imageUrl: Uri,
        currentLocation: GeoPoint,
        keepLocationPrivate: Boolean
    ) {
        val user = User(
            id = id,
            displayName = displayName,
            email = email,
            imageUrl = imageUrl,
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

    suspend fun checkTodoItem(userId: String, todoId: String) {
        val doc = usersCollection.document(userId)
        db.runTransaction { transaction ->
            val user = transaction.get(doc).toObject(User::class.java)
            val item = user?.todoList?.find { (it.id == todoId) }
            if (item != null) {
                item.completed = true
            }
            val updatedTodoList = user?.todoList ?: listOf()
            transaction.update(doc, "todoList", updatedTodoList)
        }.await()
    }

    suspend fun unCheckTodoItem(userId: String, todoId: String) {
        val doc = usersCollection.document(userId)
        db.runTransaction { transaction ->
            val user = transaction.get(doc).toObject(User::class.java)
            val item= user?.todoList?.find { (it.id == todoId) }
            if (item != null) {
                item.completed = false
            }
            val updatedTodoList = user?.todoList ?: listOf()
            transaction.update(doc, "todoList", updatedTodoList)
        }.await()
    }

    suspend fun deleteTodoItem(userId: String, todoId: String) {
        val doc = usersCollection.document(userId)
        db.runTransaction { transaction ->
            val user = transaction.get(doc).toObject(User::class.java)
            val updatedTodoList = user?.todoList?.filterNot { it.id == todoId } ?: listOf()
            transaction.update(doc, "todoList", updatedTodoList)
        }.await()
    }
}