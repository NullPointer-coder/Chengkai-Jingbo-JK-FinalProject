package com.example.smartreciperecommenderapp.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.tasks.await

class AuthHelper(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    /**
     * Re-authenticate the user with their credentials.
     * @param email The user's email.
     * @param password The user's password.
     * @return True if re-authentication succeeds, otherwise false.
     */
    suspend fun refreshSessionIfNeeded(email: String, password: String): Boolean {
        return try {
            val user = auth.currentUser

            if (user != null) {
                // Reauthenticate the current user
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()
                true // Reauthentication successful
            } else {
                // User not logged in or does not exist
                println("No user currently logged in.")
                false
            }
        } catch (e: Exception) {
            println("Error during reauthentication: ${e.message}")
            false // Reauthentication failed
        }
    }

}