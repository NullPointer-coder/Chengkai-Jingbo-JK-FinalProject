package com.example.smartreciperecommenderapp.data.repository


import android.util.Log
import com.example.smartreciperecommenderapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
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
            val userId = result.user?.uid ?: return RegisterResult.Failure("User ID is null.")

            Log.d("FirebaseAuth", "createUserWithEmail:success")

            // 将用户信息存储到 Firestore
            val user = User(username = username, email = email, password = password)
            usersCollection.document(userId).set(user).await()

            // 发送邮箱验证邮件
            result.user?.sendEmailVerification()?.await()
            Log.d("FirebaseAuth", "Email verification sent to: ${result.user?.email}")

            // 注册成功
            RegisterResult.Success
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("email address is already in use") == true -> "Email already in use"
                e.message?.contains("The email address is badly formatted") == true -> "Invalid email format"
                e.message?.contains("at least 6 characters") == true -> "Password should be at least 6 characters"
                else -> "${e.message}"
            }
            RegisterResult.Failure(errorMessage)
        }
    }


    // Log in user
    suspend fun loginUser(email: String, password: String): LoginResult {
        return try {
            // 使用 Firebase 的 signIn 方法进行用户登录
            val result = auth.signInWithEmailAndPassword(email, password).await()
            println("signInWithEmailAndPassword result: $result")
            // 登录成功，返回成功结果
            Log.d("FirebaseAuth", "signInWithEmail:success")
            val user = result.user

            // 检查是否需要验证电子邮件
            if (user != null && !user.isEmailVerified) {
                Log.w("FirebaseAuth", "Email not verified: ${user.email}")
                return LoginResult.Error("Email not verified. Please verify your email.")
            }

            LoginResult.Success("Login successful! Welcome back, ${user?.email}")
        } catch (e: Exception) {
            // 捕获异常并根据异常信息分类错误
            when {
                e.message?.contains("There is no user record") == true -> {
                    Log.e("FirebaseAuth", "No user record found for this email.", e)
                    LoginResult.UserNotFound
                }
                e.message?.contains("The password is invalid") == true -> {
                    Log.e("FirebaseAuth", "Incorrect password.", e)
                    LoginResult.Error("Login failed: Incorrect password.")
                }
                e.message?.contains("blocked all requests from this device") == true -> {
                    Log.e("FirebaseAuth", "ReCAPTCHA blocked requests.", e)
                    LoginResult.Error("Too many requests. Please try again later.")
                }
                else -> {
                    Log.e("FirebaseAuth", "Login failed due to unexpected error: ${e.message}", e)
                    LoginResult.Error("Login failed: ${e.message}")
                }
            }
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
    data class Success(val message: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
    data object UserNotFound : LoginResult()
}

sealed class RegisterResult {
    data object Success : RegisterResult()
    data class Failure(val errorMessage: String) : RegisterResult()
}