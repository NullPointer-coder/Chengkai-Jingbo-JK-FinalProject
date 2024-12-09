package com.example.smartreciperecommenderapp.ui.ProfileScreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.repository.LoginResult
import com.example.smartreciperecommenderapp.data.repository.RegisterResult
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Temporary credentials for registration/login
    private val _temporaryEmail = MutableLiveData("")
    val temporaryEmail: LiveData<String> get() = _temporaryEmail

    private val _temporaryPassword = MutableLiveData("")
    val temporaryPassword: LiveData<String> get() = _temporaryPassword

    // Login status and results
    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _isEmailVerified = MutableLiveData(false)
    val isEmailVerified: LiveData<Boolean> get() = _isEmailVerified

    private val _loginResult = MutableLiveData<LoginResult?>()
    val loginResult: LiveData<LoginResult?> get() = _loginResult

    // User information
    val userName = MutableLiveData("")
    val userAvatarUrl = MutableLiveData<String?>(null)

    // Navigation handlers
    var navigateToMyFavorite: () -> Unit = {}
    var navigateToFavoriteCuisines: () -> Unit = {}
    var navigateToSettings: () -> Unit = {}

    init {
        checkLoginStatus()
    }

    /** Check login and email verification status on initialization. */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = userRepository.isUserLoggedIn()
            _isLoggedIn.value = isLoggedIn

            if (isLoggedIn) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                checkEmailVerificationStatus(currentUser)
                if (_isEmailVerified.value == true) {
                    fetchUserDetails()
                } else {
                    _isLoggedIn.value = false // Block login if email is not verified
                }
            }
        }
    }

    fun checkEmailVerification() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            viewModelScope.launch {
                try {
                    it.reload().await() // 刷新用户信息
                    _isEmailVerified.value = it.isEmailVerified // 更新验证状态
                } catch (e: Exception) {
                    _isEmailVerified.value = false // 处理异常
                    println("Error checking email verification: ${e.message}")
                }
            }
        } ?: run {
            _isEmailVerified.value = false // 如果用户为 null，则设置为未验证
        }
    }


    /** Check the current user's email verification status. */
    private suspend fun checkEmailVerificationStatus(user: FirebaseUser?) {
        try {
            if (user != null) {
                user.reload().await() // 刷新用户信息
                _isEmailVerified.value = user.isEmailVerified // 更新验证状态
            } else {
                _isEmailVerified.value = false // 如果用户为 null，则默认未验证
            }
        } catch (e: Exception) {
            // 捕获异常并打印日志
            println("Error checking email verification status: ${e.message}")
            _isEmailVerified.value = false // 如果发生异常，安全地设置为未验证
        }
    }

    /** Update temporary credentials for the session. */
    fun updateTemporaryCredentials(email: String, password: String) {
        _temporaryEmail.value = email
        _temporaryPassword.value = password
    }

    /** Perform user login. */
    fun login(email: String, password: String, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = userRepository.loginUser(email, password)
                _loginResult.value = result

                if (result is LoginResult.Success) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    checkEmailVerificationStatus(currentUser)

                    if (_isEmailVerified.value == true) {
                        _isLoggedIn.value = true
                    } else {
                        onFailure("Email not verified. Please verify your email.")
                        _isLoggedIn.value = false
                    }
                } else if (result is LoginResult.Error) {
                    onFailure(result.errorMessage)
                }
            } catch (e: Exception) {
                onFailure("An unexpected error occurred: ${e.message}")
            }
        }
    }

    /** Resend email verification. */
    fun resendVerificationEmail(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                val errorMessage = task.exception?.message ?: "Failed to resend email."
                onFailure(errorMessage)
            }
        }
    }

    /** Register a new user. */
    fun registerUser(
        email: String,
        password: String,
        username: String,
        onEmailVerificationPending: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = userRepository.registerUser(email, password, username)
                if (result is RegisterResult.Success) {
                    _isEmailVerified.value = result.isEmailVerified

                    if (result.isEmailVerified) {
                        _isLoggedIn.value = true
                    } else {
                        onEmailVerificationPending()
                        _isLoggedIn.value = false
                    }
                } else if (result is RegisterResult.Failure) {
                    onFailure(result.errorMessage)
                }
            } catch (e: Exception) {
                onFailure("An unexpected error occurred: ${e.message}")
            }
        }
    }

    /** Fetch user details after login and email verification. */
    fun fetchUserDetails(onDetailsFetched: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser

                // 确保用户不为 null 并提取 displayName
                userName.value = user?.displayName ?: "Guest"
                userAvatarUrl.value = user?.photoUrl?.toString() // 如果没有头像 URL，这里可能是 null

                onDetailsFetched?.invoke() // 通知详情加载完成
            } catch (e: Exception) {
                userName.value = "Guest"
                userAvatarUrl.value = null
            }
        }
    }

    fun updateDisplayName(newDisplayName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.updateUserDisplayName(newDisplayName)
            if (success) {
                // 更新本地 userName
                userName.value = newDisplayName
                onSuccess()
            } else {
                onFailure("Failed to update display name.")
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.sendPasswordResetEmail(email)
            if (result) {
                onSuccess()
            } else {
                onFailure("Failed to send password reset email. Please check if the email is correct.")
            }
        }
    }




    /** Reset login result. */
    fun resetLoginResult() {
        _loginResult.value = null
    }

    /** Logout the user. */
    fun logout() {
        userRepository.logoutUser()
        _isLoggedIn.value = false
        _isEmailVerified.value = false // Reset email verification status
        resetLoginResult() // Clear login result
    }

    /** Set navigation handlers for screens. */
    fun setNavigationHandlers(
        onMyFavorite: () -> Unit,
        onFavoriteCuisines: () -> Unit,
        onSettings: () -> Unit
    ) {
        navigateToMyFavorite = onMyFavorite
        navigateToFavoriteCuisines = onFavoriteCuisines
        navigateToSettings = onSettings
    }

    fun updateUserName(newUserName: String) {
        userName.value = newUserName
    }
}
