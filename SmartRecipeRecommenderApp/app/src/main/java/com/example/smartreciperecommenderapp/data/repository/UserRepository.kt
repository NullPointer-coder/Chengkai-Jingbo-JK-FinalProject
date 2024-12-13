package com.example.smartreciperecommenderapp.data.repository

import android.util.Log
import com.example.smartreciperecommenderapp.data.model.RecipeDetailModel
import com.example.smartreciperecommenderapp.data.model.RecipeModel
import com.example.smartreciperecommenderapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val authHelper = AuthHelper(auth)

    private val realtimeDatabase = FirebaseDatabase.getInstance()

    suspend fun fetchUserFavoriteRecipeDetail(uid: String): List<RecipeDetailModel> {
        return try {
            val favoritesRef = realtimeDatabase.getReference("users").child(uid).child("favorites")
            val dataSnapshot = favoritesRef.get().await()

            if (!dataSnapshot.exists()) {
                // No favorites node
                return emptyList()
            }

            val resultList = mutableListOf<RecipeDetailModel>()
            for (childSnapshot in dataSnapshot.children) {
                val recipe = childSnapshot.getValue(RecipeDetailModel::class.java)
                if (recipe != null) {
                    resultList.add(recipe)
                }
            }
            Log.d("UserRepository", "resultList: $resultList")
            resultList
        } catch (e: Exception) {
            println("Error fetching favorites: ${e.message}")
            emptyList()
        }
    }

    /**
     * Check if the user is currently logged in.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Re-authenticate the user if the session has expired.
     *
     * @return true if session refreshed successfully, false otherwise.
     */
    suspend fun refreshSessionIfNeeded(email: String, password: String): Boolean {
        return authHelper.refreshSessionIfNeeded(email, password)
    }

    /**
     * Register a new user with the given email, password, and username.
     * Sends a verification email after successful registration.
     *
     * @return a RegisterResult indicating success or failure.
     */
    suspend fun registerUser(email: String, password: String, username: String): RegisterResult {
        return try {
            // Create user
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return RegisterResult.Failure("User ID is null.")

            Log.d("FirebaseAuth", "createUserWithEmail:success")

            // Update user's display name
            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }
            user.updateProfile(profileUpdates).await()

            // Send verification email
            user.sendEmailVerification().await()
            Log.d("FirebaseAuth", "Verification email sent to ${user.email}")

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

    /**
     * Check if a user exists in Firebase by attempting to create and delete a temporary account.
     *
     * @return true if the user already exists, false otherwise.
     */
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
                e.message?.contains("already in use") == true -> true // User already exists
                else -> {
                    println("Error checking user existence: ${e.message}")
                    false
                }
            }
        }
    }

    /**
     * Log in a user with email and password.
     *
     * @return a LoginResult indicating success, error, or user not found.
     */
    suspend fun loginUser(email: String, password: String): LoginResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                user.reload().await() // Ensure user info is up to date
                return if (user.isEmailVerified) {
                    LoginResult.Success
                } else {
                    LoginResult.Error("Your email is not verified. Please check your inbox.")
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

    /**
     * Retrieve the currently logged-in user's details from Firestore.
     *
     * @return a User object or null if no user is logged in or an error occurs.
     */
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

    /**
     * Update the display name of the currently logged-in user.
     *
     * @return true if the update was successful, false otherwise.
     */
    suspend fun updateUserDisplayName(newDisplayName: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        return try {
            val profileUpdates = userProfileChangeRequest {
                displayName = newDisplayName
            }
            currentUser.updateProfile(profileUpdates).await()
            true
        } catch (e: Exception) {
            println("Error updating display name: ${e.message}")
            false
        }
    }

    /**
     * Send a password reset email to the given email address.
     *
     * @return true if the email was sent successfully, false otherwise.
     */
    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            println("Error sending password reset email: ${e.message}")
            false
        }
    }

    /**
     * Log out the currently logged-in user.
     */
    fun logoutUser() {
        auth.signOut()
        Log.d("FirebaseAuth", "User logged out successfully.")
    }
}

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val errorMessage: String) : LoginResult()
    object UserNotFound : LoginResult()
}

sealed class RegisterResult {
    data class Success(val isEmailVerified: Boolean) : RegisterResult()
    data class Failure(val errorMessage: String) : RegisterResult()
}
