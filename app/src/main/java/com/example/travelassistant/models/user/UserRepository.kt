package com.example.travelassistant.models.user

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val PROMPT_LIMIT = 20

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
            imageUrl = imageUrl.toString(),
            currentLocation = currentLocation,
            keepLocationPrivate = keepLocationPrivate,
            promptCount = PROMPT_LIMIT
        )
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun updateSettings(userId: String, location: GeoPoint, keepLocationPrivate: Boolean) {
        val updates = hashMapOf<String, Any>(
            "currentLocation" to location,
            "keepLocationPrivate" to keepLocationPrivate
        )
        usersCollection.document(userId).update(updates).await()
    }

    suspend fun updatePoints(userId: String, newPoints: Int) {
        usersCollection.document(userId).update("points", newPoints).await()
    }

    suspend fun decreasePromptCount(userId: String, newCount: Int) {
        usersCollection.document(userId).update("promptCount", newCount).await()
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