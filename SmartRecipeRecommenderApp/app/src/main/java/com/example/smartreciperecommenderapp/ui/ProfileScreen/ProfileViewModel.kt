package com.example.smartreciperecommenderapp.ui.ProfileScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.repository.LoginResult
import com.example.smartreciperecommenderapp.data.repository.RegisterResult
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    var temporaryEmail = MutableLiveData<String>("")
    var temporaryPassword = MutableLiveData<String>("")

    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _loginResult = MutableLiveData<LoginResult?>()
    val loginResult: LiveData<LoginResult?> get() = _loginResult

    val userName = MutableLiveData<String>("")
    val userAvatarUrl = MutableLiveData<String?>(null)

    var navigateToMyFavorite: () -> Unit = {}
    var navigateToFavoriteCuisines: () -> Unit = {}
    var navigateToSettings: () -> Unit = {}

    init {
        // 初始化时检查登录状态
        _isLoggedIn.value = userRepository.isUserLoggedIn()
        if (_isLoggedIn.value == true) {
            fetchUserDetails() // Load user details if logged in
        }
    }

    fun getTemporaryEmail(): String {
        return temporaryEmail.value ?: ""
    }

    // Function to get the current temporary password
    fun getTemporaryPassword(): String {
        return temporaryPassword.value ?: ""
    }

    fun getUserName(): String {
        return userName.value ?: ""
    }

    fun updateUserName(tempUserName: String) {
        userName.value = tempUserName
    }
    fun updateTemporaryCredentials(email: String, password: String) {
        temporaryEmail.value = email
        temporaryPassword.value = password
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            println("result111111:$result")
            _loginResult.value = result
            if (result is LoginResult.Success) {
                _isLoggedIn.value = true
            }
        }
    }

    fun registerUser(email: String, password: String, username: String, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "registerUser called with email=$email, password=$password, username=$username")
            when (val result = userRepository.registerUser(email, password, username)) {
                is RegisterResult.Success -> {
                    Log.d("ProfileViewModel", "User registered successfully!")
                    fetchUserDetails()
                }
                is RegisterResult.Failure -> {
                    Log.d("ProfileViewModel", "User registration failed: ${result.errorMessage}")
                    // Pass a simplified error message to the UI
                    onFailure(result.errorMessage.split(":").firstOrNull() ?: "Registration failed")
                }
            }
        }
    }



    private fun fetchUserDetails() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            userName.value = user?.username ?: ""
            userAvatarUrl.value = user?.avatarUrl
        }
    }

    fun performSensitiveAction(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Reauthenticate the user
                val reauthenticated = userRepository.refreshSessionIfNeeded(email, password)
                val currentUser = userRepository.getCurrentUser() // Fetch user after reauthentication
                if (reauthenticated && currentUser != null) {
                    onSuccess()
                } else {
                    onFailure("Re-authentication failed or user does not exist.")
                }

            } catch (e: Exception) {
                // Log the error and provide user feedback
                println("Re-authentication exception: ${e.message}")
                onFailure("An error occurred during re-authentication: ${e.message}")
            }
        }
    }

    fun updateLoginResult(result: LoginResult) {
        _loginResult.value = result
    }

    fun resetLoginResult() {
        _loginResult.value = null
    }


    fun logout() {
        userRepository.logoutUser()
        _isLoggedIn.value = false
    }


    fun setNavigationHandlers(
        onMyFavorite: () -> Unit,
        onFavoriteCuisines: () -> Unit,
        onSettings: () -> Unit
    ) {
        navigateToMyFavorite = onMyFavorite
        navigateToFavoriteCuisines = onFavoriteCuisines
        navigateToSettings = onSettings
    }


}