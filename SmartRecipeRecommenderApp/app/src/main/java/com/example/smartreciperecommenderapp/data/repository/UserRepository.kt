package com.example.smartreciperecommenderapp.data.repository


import android.util.Log
import com.example.smartreciperecommenderapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val authHelper = AuthHelper(auth)

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }


    // Re-authenticate the user (if the session has expired)
    suspend fun refreshSessionIfNeeded(email: String, password: String): Boolean {
        // Attempt to refresh the session token or re-authenticate
        return authHelper.refreshSessionIfNeeded(email, password)
    }

    // Register user
    suspend fun registerUser(email: String, password: String, username: String): RegisterResult {
        return try {
            // 创建用户
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return RegisterResult.Failure("User ID is null.")

            Log.d("FirebaseAuth", "createUserWithEmail:success")

            // 更新用户的 Display Name
            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }
            user.updateProfile(profileUpdates).await()

            user.sendEmailVerification().await()
            Log.d("FirebaseAuth", "Verification email sent to ${user.email}")

            // 注册成功，直接返回成功状态
            RegisterResult.Success(isEmailVerified = user.isEmailVerified)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("email address is already in use") == true -> "Email already in use"
                e.message?.contains("The email address is badly formatted") == true -> "Invalid email format"
                e.message?.contains("at least 6 characters") == true -> "Password should be at least 6 characters"
                else -> e.message ?: "Unknown error occurred"
            }
            RegisterResult.Failure(errorMessage)
        }
    }

    private suspend fun isUserInFirebase(email: String): Boolean {
        return try {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, "temporary_password")
                .await()

            FirebaseAuth.getInstance().currentUser?.delete()?.await()
            false
        } catch (e: Exception) {
            println("isUserInFirebase e.message: ${e.message}")
            when {
               e.message?.contains("already in use") == true -> true // 用户已存在
                else -> {
                    println("Error checking user existence: ${e.message}")
                    false
                }
            }
        }
    }

    // Log in user
    suspend fun loginUser(email: String, password: String): LoginResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                user.reload().await() // 确保用户信息最新
                return if (user.isEmailVerified) {
                    LoginResult.Success
                } else {
                    LoginResult.Error("Your email is not verified. Please check your inbox.") // 邮箱未激活
                }
            } else {
                LoginResult.UserNotFound
            }
        } catch (e: Exception) {
            println("e.message: ${e.message}")
            println("isUserInFirebase: ${isUserInFirebase(email)}")
            val errorMessage = when {
                e.message?.contains("The supplied auth credential is incorrect") == true && isUserInFirebase(email) -> "Email or password is incorrect"
                e.message?.contains("The supplied auth credential is incorrect") == true && !isUserInFirebase(email) -> return LoginResult.UserNotFound
                e.message?.contains("Given String is empty or null") == true -> "Email or password cannot be empty"
                else -> e.message ?: "Unknown error occurred"
            }
            LoginResult.Error(errorMessage)
        }
    }


    // Get the current logged-in user details
    suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            println("Error fetching current user: ${e.message}")
            null
        }
    }

    // Log out the user
    fun logoutUser() {
        auth.signOut()
        Log.d("FirebaseAuth", "User logged out successfully.")
    }
}

sealed class LoginResult {
    data object Success : LoginResult()
    data class Error(val errorMessage: String) : LoginResult()
    data object UserNotFound : LoginResult()
}

sealed class RegisterResult {
    data class Success(val isEmailVerified: Boolean) : RegisterResult()
    data class Failure(val errorMessage: String) : RegisterResult()
}