package com.example.smartreciperecommenderapp.data.repository


import com.example.smartreciperecommenderapp.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

open class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // 获取用户数据
    suspend fun getUser(userId: String): User? {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            println("Error getting user: ${e.message}")
            null
        }
    }

    // 保存用户数据
    suspend fun saveUser(userId: String, user: User) {
        try {
            usersCollection.document(userId).set(user).await()
            println("User saved successfully")
        } catch (e: Exception) {
            println("Error saving user: ${e.message}")
        }
    }

    // 删除用户数据
    suspend fun deleteUser(userId: String) {
        try {
            usersCollection.document(userId).delete().await()
            println("User deleted successfully")
        } catch (e: Exception) {
            println("Error deleting user: ${e.message}")
        }
    }
}
