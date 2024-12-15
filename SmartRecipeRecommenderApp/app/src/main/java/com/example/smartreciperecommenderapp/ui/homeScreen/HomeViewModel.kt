package com.example.smartreciperecommenderapp.ui.homeScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.data.model.RecipeDetailModel
import com.example.smartreciperecommenderapp.data.model.RecipeModel
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.data.repository.RecipeDetailRepository
import com.example.smartreciperecommenderapp.data.repository.RecipeRepository
import com.example.smartreciperecommenderapp.ui.IngredientScreen.calculateRemainingDays
import com.example.smartreciperecommenderapp.ui.api.DirectionsWrapper
import com.example.smartreciperecommenderapp.ui.api.FatSecretService
import com.example.smartreciperecommenderapp.ui.api.GoogleImageSearchService
import com.example.smartreciperecommenderapp.ui.api.GoogleSearchConfig
import com.example.smartreciperecommenderapp.ui.api.IngredientsWrapper
import com.example.smartreciperecommenderapp.ui.api.RecipeCategoriesWrapper
import com.example.smartreciperecommenderapp.ui.api.RecipeTypesWrapper
import com.example.smartreciperecommenderapp.ui.api.RetrofitInstance
import com.example.smartreciperecommenderapp.ui.api.ServingSizesWrapper
import com.example.smartreciperecommenderapp.utils.AnalyticsLogger
import com.example.smartreciperecommenderapp.utils.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class HomeViewModel(
    private val ingredientRepository: IngredientRepository,
    private val fatSecretService: FatSecretService,
    private val recipeRepository: RecipeRepository,
    private val recipeDetailRepository: RecipeDetailRepository,
    private val networkMonitor: NetworkMonitor,
    private val analyticsLogger: AnalyticsLogger
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeModel>>(emptyList())
    val recipes = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Now using RecipeDetailModel for selected details
    private val _selectedRecipeDetails = MutableStateFlow<RecipeDetailModel?>(null)
    val selectedRecipeDetails = _selectedRecipeDetails.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Google Image Search service for fallback images
    private val googleImageSearchService = GoogleImageSearchService(
        apiKey = GoogleSearchConfig().apiKey,
        cseCx = GoogleSearchConfig().cseCx
    )

    private suspend fun getUniqueNonExpiredIngredients(): List<Ingredient> {
        return ingredientRepository.getAllIngredientsFromRoom()
            .filter { it.expiryDate?.let { date -> calculateRemainingDays(date) >= 0 } ?: true }
            .distinctBy { it.id }
    }

    private val gson = com.google.gson.Gson()

    // Function to parse categories JSON
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

    fun loadRecipeDetails(recipeId: Long) {
        Log.d("HomeViewModel", "Loading recipe details for ID: $recipeId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (recipeId == 0L) {
                    Log.d("HomeViewModel", "recipeId == 0L, clearing details.")
                    // Clear details
                    _selectedRecipeDetails.value = null
                    return@launch
                }

                Log.d("HomeViewModel", "Checking local details from DB...")
                val localDetails = recipeDetailRepository.getRecipeDetailsFromDB(recipeId)
                if (localDetails != null) {
                    Log.d("HomeViewModel", "Loaded recipe details from local DB. localDetails: $localDetails")
                    // We have cached details, now fetch imageUrl from recipeRepository
                    val imageUrl = recipeRepository.getRecipeImageUrlById(recipeId)
                    val updatedDetails = if (imageUrl != null) {
                        Log.d("HomeViewModel", "Found imageUrl from repo: $imageUrl")
                        localDetails.copy(imageUrl = imageUrl)
                    } else {
                        Log.d("HomeViewModel", "No imageUrl found from repo.")
                        localDetails
                    }
                    _selectedRecipeDetails.value = updatedDetails

                    //Tracking the user's browsing
                    analyticsLogger.logViewRecipe(updatedDetails.name)
                } else {
                    Log.d("HomeViewModel", "No local details found. Need network.")
                    // No local details, need network
                    if (!networkMonitor.isConnected.value) {
                        Log.d("HomeViewModel", "No network, returning early.")
                        _errorMessage.value = "No network connection available."
                        return@launch
                    }

                    Log.d("HomeViewModel", "Fetching access token...")
                    val tokenResponse = RetrofitInstance.fatSecretAuthApi.getAccessToken()
                    val accessToken = tokenResponse.access_token
                    Log.d("HomeViewModel", "Got access token: $accessToken")

                    Log.d("HomeViewModel", "Requesting recipe details from API...")
                    val response = fatSecretService.searchRecipesDetails(
                        authorization = "Bearer $accessToken",
                        id = recipeId
                    )

                    val details = response.recipe
                    Log.d("HomeViewModel", "Loaded recipe details from API: $details")
                    // Store details locally
                    Log.d("HomeViewModel", "Storing recipe details in DB...")
                    recipeDetailRepository.storeRecipeDetails(details)

                    Log.d("HomeViewModel", "Reading recipe details back from DB...")
                    val fromDB = recipeDetailRepository.getRecipeDetailsFromDB(recipeId)
                    Log.d("HomeViewModel", "fromDB: $fromDB")
                    if (fromDB != null) {
                        val imageUrl = recipeRepository.getRecipeImageUrlById(recipeId)
                        val finalDetails = if (imageUrl != null) {
                            Log.d("HomeViewModel", "Found imageUrl from repo: $imageUrl")
                            fromDB.copy(imageUrl = imageUrl)
                        } else {
                            Log.d("HomeViewModel", "No imageUrl found from repo after storing details.")
                            fromDB
                        }
                        _selectedRecipeDetails.value = finalDetails
                    } else {
                        Log.d("HomeViewModel", "Failed to load details from DB after storing.")
                        // In case of any unexpected error, just show what we have
                        _selectedRecipeDetails.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading recipe details", e)

                FirebaseCrashlytics.getInstance().recordException(e)

                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Network error: Unable to load recipes. Please check your internet connection."
                    is java.net.SocketTimeoutException -> "Network error: Request timed out. Please try again."
                    else -> "Failed to load recipes. Please try again later."
                }
                Log.d("HomeViewModel", "Setting _errorMessage to: $errorMessage")
                _errorMessage.value = errorMessage
            } finally {
                Log.d("HomeViewModel", "Finished loading details, isLoading = false")
                _isLoading.value = false
            }
        }
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true

            if (networkMonitor.isConnected.first()) {
                // Online: sync with Firebase
                ingredientRepository.syncIngredients()
                Log.d("HomeViewModel", "Synced ingredients with Firebase.")
            } else {
                // Offline: no sync
                Log.d("HomeViewModel", "No internet connection. Loading offline data from Room.")
                _errorMessage.value = "No network connection available."
            }

            try {
                val currentIngredients = getUniqueNonExpiredIngredients()
                Log.d("HomeViewModel", "Fetched non-expired unique ingredients: $currentIngredients")

                if (currentIngredients.isEmpty()) {
                    Log.d("HomeViewModel", "No ingredients to fetch recipes for.")
                    _recipes.value = emptyList()
                    return@launch
                }

                val currentIngredientNames = currentIngredients.map { it.name }.toSet()
                val allOriginIngredientsInDB = recipeRepository.getAllOriginIngredients().toSet()

                val addedIngredients = currentIngredientNames - allOriginIngredientsInDB
                val deletedIngredients = allOriginIngredientsInDB - currentIngredientNames

                // Handle deletions
                deletedIngredients.forEach { ingredientName ->
                    recipeRepository.deleteRecipesByIngredient(ingredientName)
                    Log.d("HomeViewModel", "Deleted old recipes for removed ingredient: $ingredientName")
                }

                // Fetch recipes only for newly added ingredients
                val allSelectedModels = mutableListOf<RecipeModel>()
                for (ingredientName in addedIngredients) {
                    if (ingredientName.isNotEmpty()) {
                        if (!networkMonitor.isConnected.value) {
                            _errorMessage.value = "No network connection available."
                            continue
                        }

                        val tokenResponse = RetrofitInstance.fatSecretAuthApi.getAccessToken()
                        val accessToken = tokenResponse.access_token

                        val response = fatSecretService.searchRecipes(
                            authorization = "Bearer $accessToken",
                            name = ingredientName,
                            maxResults = 50
                        )

                        recipeRepository.storeRecipes(response.recipes.recipe, ingredientName)
                        val newlyFetchedRecipes = recipeRepository.getRecipesByIngredient(ingredientName)
                        val selectedRecipes = newlyFetchedRecipes.shuffled().take(2)
                        allSelectedModels.addAll(selectedRecipes)
                    }
                }

                // Use existing recipes from the database for unchanged ingredients
                currentIngredientNames.subtract(addedIngredients).forEach { ingredientName ->
                    val localRecipes = recipeRepository.getRecipesByIngredient(ingredientName)
                    val selectedRecipes = localRecipes.shuffled().take(2)
                    allSelectedModels.addAll(selectedRecipes)
                }

                // Fallback image fetch if needed
                val updatedModels = allSelectedModels.map { model ->
                    if (model.imageUrl == null) {
                        val googleImage = googleImageSearchService.fetchFirstImageForFood(model.name)
                        if (googleImage != null) {
                            recipeRepository.updateRecipeImageUrl(model.id, googleImage)
                            model.copy(imageUrl = googleImage)
                        } else {
                            model
                        }
                    } else {
                        model
                    }
                }

                Log.d("HomeViewModel", "Final selected RecipeModels with fallback images: $updatedModels")
                _recipes.value = updatedModels

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading recipes", e)
                // Log the exception to Firebase Crashlytics
                FirebaseCrashlytics.getInstance().recordException(e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Network error: Unable to load recipes. Please check your internet connection."
                    is java.net.SocketTimeoutException -> "Network error: Request timed out. Please try again."
                    else -> "Failed to load recipes. Please try again later."
                }
                _errorMessage.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshRecipesLocally() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ingredients = getUniqueNonExpiredIngredients()
                val allSelectedModels = mutableListOf<RecipeModel>()
                // Fetch recipes for each ingredient
                for (ingredient in ingredients) {
                    val ingredientName = ingredient.name
                    if (ingredientName.isNotEmpty()) {
                        val localModels = recipeRepository.getRecipesByIngredient(ingredientName)
                        val selected = localModels.shuffled().take(2)
                        allSelectedModels.addAll(selected)
                    }
                }
                val updatedModels = allSelectedModels.map { model ->
                    if (model.imageUrl == null) {
                        val googleImage = googleImageSearchService.fetchFirstImageForFood(model.name)
                        if (googleImage != null) {
                            recipeRepository.updateRecipeImageUrl(model.id, googleImage)
                            model.copy(imageUrl = googleImage)
                        } else {
                            model
                        }
                    } else {
                        model
                    }
                }
                _recipes.value = updatedModels
            } catch (e: Exception) {
                // Log the exception to Firebase Crashlytics
                FirebaseCrashlytics.getInstance().recordException(e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "Network error: Unable to load recipes. Please check your internet connection."
                    is java.net.SocketTimeoutException -> "Network error: Request timed out. Please try again."
                    else -> "Failed to load recipes. Please try again later."
                }
                _errorMessage.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun saveMyFavoriteRecipe(recipe: RecipeDetailModel) {
        Log.d("HomeViewModel", "Saving recipe image Url: ${recipe.imageUrl}")
        viewModelScope.launch {
            if (!networkMonitor.isConnected.value) {
                // No network connection, handle this case as needed:
                // e.g., show a message or log it.
                Log.e("HomeViewModel", "Cannot save favorite without network connection.")
                return@launch
            }

            val userId = getCurrentUserId()
            val database = FirebaseDatabase.getInstance()
            val favoritesRef = userId?.let { database.getReference("users").child(it).child("favorites") }

            favoritesRef?.child(recipe.recipeId.toString())?.setValue(recipe)?.addOnSuccessListener {
                Log.d("HomeViewModel", "Recipe saved to Firebase favorites.")
            }?.addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("HomeViewModel", "Failed to save recipe to Firebase: ", e)
            }
        }
    }

    suspend fun isRecipeFavorite(recipeId: Long): Boolean {
        return suspendCancellableCoroutine { cont ->
            val userId = getCurrentUserId()
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
            if (!networkMonitor.isConnected.value) {
                // No network connection, handle this case as needed
                Log.e("HomeViewModel", "Cannot remove favorite without network connection.")
                return@launch
            }

            val userId = getCurrentUserId()
            val database = FirebaseDatabase.getInstance()
            val favoritesRef = userId?.let { database.getReference("users").child(it).child("favorites") }

            favoritesRef?.child(recipe.recipeId.toString())?.removeValue()?.addOnSuccessListener {
                Log.d("HomeViewModel", "Recipe removed from Firebase favorites.")
            }?.addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("HomeViewModel", "Failed to remove recipe from Firebase: ", e)
            }
        }
    }

    private fun getCurrentUserId(): String? {
         return FirebaseAuth.getInstance().currentUser?.uid
    }
}
