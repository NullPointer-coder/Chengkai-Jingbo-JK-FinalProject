package com.example.smartreciperecommenderapp.ui.ProfileScreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.RecipeDetailModel
import com.example.smartreciperecommenderapp.data.model.RecipeModel
import com.example.smartreciperecommenderapp.data.repository.LoginResult
import com.example.smartreciperecommenderapp.data.repository.RegisterResult
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.ui.api.DirectionsWrapper
import com.example.smartreciperecommenderapp.ui.api.IngredientsWrapper
import com.example.smartreciperecommenderapp.ui.api.RecipeCategoriesWrapper
import com.example.smartreciperecommenderapp.ui.api.RecipeTypesWrapper
import com.example.smartreciperecommenderapp.ui.api.ServingSizesWrapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

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

    private val _favorites = MutableLiveData<List<RecipeModel>>(emptyList())
    val favorites: LiveData<List<RecipeModel>> get() = _favorites

    private val _favoriteDetail = MutableLiveData<List<RecipeDetailModel>>(emptyList())
    val favoriteDetail: LiveData<List<RecipeDetailModel>> get() = _favoriteDetail

    private val gson = com.google.gson.Gson()

    init {
        checkLoginStatus()
    }

    fun getCategories(recipeDetails: RecipeDetailModel?): RecipeCategoriesWrapper? {
        return recipeDetails?.categoriesJson?.let {
            gson.fromJson(it, RecipeCategoriesWrapper::class.java)
        }
    }

    // Function to parse types JSON
    fun getTypes(recipeDetails: RecipeDetailModel?): RecipeTypesWrapper? {
        return recipeDetails?.typesJson?.let {
            gson.fromJson(it, RecipeTypesWrapper::class.java)
        }
    }

    // Function to parse serving sizes JSON
    fun getServingSizes(recipeDetails: RecipeDetailModel?): ServingSizesWrapper? {
        return recipeDetails?.servingSizesJson?.let {
            gson.fromJson(it, ServingSizesWrapper::class.java)
        }
    }

    // Function to parse ingredients JSON
    fun getIngredients(recipeDetails: RecipeDetailModel?): IngredientsWrapper {
        return recipeDetails?.ingredientsJson?.let {
            gson.fromJson(it, IngredientsWrapper::class.java)
        } ?: IngredientsWrapper(emptyList())
    }

    // Function to parse directions JSON
    fun getDirections(recipeDetails: RecipeDetailModel?): DirectionsWrapper {
        return recipeDetails?.directionsJson?.let {
            gson.fromJson(it, DirectionsWrapper::class.java)
        } ?: DirectionsWrapper(emptyList())
    }

    private fun loadUserFavorites() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val favoriteDetailList = userRepository.fetchUserFavoriteRecipeDetail(user.uid)
                    _favoriteDetail.value = favoriteDetailList
                    Log.d("ProfileViewModel", "FavoriteDetail loaded: ${favoriteDetailList}")

                    // Map the favoriteDetailList to a list of RecipeModel
                    val mappedFavorites = favoriteDetailList.map { detail ->
                        RecipeModel(
                            id = detail.recipeId,
                            name = detail.name,
                            description = detail.description,
                            imageUrl = detail.imageUrl
                        )
                    }
                    _favorites.value = mappedFavorites
                } else {
                    _favorites.value = emptyList()
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                println("Error loading favorites: ${e.message}")
                _favorites.value = emptyList()
            }
        }
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
                    it.reload().await()
                    _isEmailVerified.value = it.isEmailVerified
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    _isEmailVerified.value = false
                    println("Error checking email verification: ${e.message}")
                }
            }
        } ?: run {
            _isEmailVerified.value = false
        }
    }


    /** Check the current user's email verification status. */
    private suspend fun checkEmailVerificationStatus(user: FirebaseUser?) {
        try {
            if (user != null) {
                user.reload().await() // Refresh user information
                _isEmailVerified.value = user.isEmailVerified // Update validation status
            } else {
                _isEmailVerified.value = false // If the user is null, the default is unauthenticated
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            println("Error checking email verification status: ${e.message}")
            _isEmailVerified.value = false // Safely set to unverified if an exception occurs
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
                FirebaseCrashlytics.getInstance().recordException(e)
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
                FirebaseCrashlytics.getInstance().recordException(e)
                onFailure("An unexpected error occurred: ${e.message}")
            }
        }
    }

    /** Fetch user details after login and email verification. */
    fun fetchUserDetails(onDetailsFetched: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser


                userName.value = user?.displayName ?: "Guest"
                userAvatarUrl.value = user?.photoUrl?.toString()

                loadUserFavorites()
                onDetailsFetched?.invoke()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
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

    fun saveMyFavoriteRecipe(recipe: RecipeDetailModel) {
        Log.d("HomeViewModel", "Saving recipe image Url: ${recipe.imageUrl}")
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val database = FirebaseDatabase.getInstance()
            val favoritesRef = userId?.let { database.getReference("users").child(it).child("favorites") }

            favoritesRef?.child(recipe.recipeId.toString())?.setValue(recipe)?.addOnSuccessListener {
                Log.d("HomeViewModel", "Recipe saved to Firebase favorites.")
            }?.addOnFailureListener { e ->
                Log.e("HomeViewModel", "Failed to save recipe to Firebase: ", e)
            }
        }
    }
    suspend fun isRecipeFavorite(recipeId: Long): Boolean {
        return suspendCancellableCoroutine { cont ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val favoritesRef = userId?.let {
                FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(it)
                    .child("favorites")
                    .child(recipeId.toString())
            }

            favoritesRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // If the snapshot exists, it means the recipe is favorite
                    cont.resume(snapshot.exists()) { _, _, _ -> }
                }

                override fun onCancelled(error: DatabaseError) {
                    // If cancelled or error occurs, consider the recipe not favorite
                    // or handle error accordingly.
                    cont.resumeWithException(error.toException())
                }
            })
        }
    }

    fun removeRecipeFromFavorite(recipe: RecipeDetailModel) {
        viewModelScope.launch {

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val database = FirebaseDatabase.getInstance()
            val favoritesRef = userId?.let { database.getReference("users").child(it).child("favorites") }

            favoritesRef?.child(recipe.recipeId.toString())?.removeValue()?.addOnSuccessListener {
            }?.addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("ProfileViewModel", "Failed to remove recipe from Firebase: ", e)
            }
        }
    }

}
