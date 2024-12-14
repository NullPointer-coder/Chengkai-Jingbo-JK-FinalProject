package com.example.smartreciperecommenderapp

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.smartreciperecommenderapp.data.model.AppDatabase
import com.example.smartreciperecommenderapp.data.repository.FirebaseIngredientService
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.data.repository.RecipeDetailRepository
import com.example.smartreciperecommenderapp.data.repository.RecipeRepository
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.ui.BottomNavigationBar
import android.Manifest
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModelFactory
import com.example.smartreciperecommenderapp.ui.api.RetrofitInstance
import com.example.smartreciperecommenderapp.ui.homeScreen.HomeViewModelFactory
import com.example.smartreciperecommenderapp.ui.navigation.NavGraph
import com.example.smartreciperecommenderapp.ui.theme.SmartRecipeRecommenderAppTheme
import com.example.smartreciperecommenderapp.utils.NetworkMonitor
import com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    private lateinit var ingredientRepository: IngredientRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val networkMonitor = NetworkMonitor(this)

        FirebaseApp.initializeApp(this)

        requestNotificationPermissionIfNeeded()

        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
            }

        applicationContext.deleteDatabase("smart_recipe_db")
        // Initialize the database and dependencies in onCreate// 在onCreate中初始化数据库和依赖
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smart_recipe_db"
        ).build()

        // Initialize IngredientRepository
        val firebaseIngredientService = FirebaseIngredientService()
        val ingredientDao = database.ingredientDao()
        ingredientRepository = IngredientRepository(ingredientDao, firebaseIngredientService)

        // Initialize RecipeRepository
        val recipeDao = database.recipeDao()
        val recipeRepository = RecipeRepository(recipeDao)

        // Initialize RecipeDetailRepository
        val recipeDetailDao = database.recipeDetailDao()
        val recipeDetailRepository = RecipeDetailRepository(recipeDetailDao)

        // Initialize UserRepository and ProfileViewModel
        val userRepository = UserRepository()
        val profileViewModel: ProfileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(userRepository)
        )[ProfileViewModel::class.java]

        enableEdgeToEdge()

        setContent {
            SmartRecipeRecommenderAppTheme {
                val navController = rememberNavController()


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        profileViewModel = profileViewModel,
                        ingredientRepository = ingredientRepository,
                        recipeRepository = recipeRepository,
                        recipeDetailRepository = recipeDetailRepository,
                        networkMonitor = networkMonitor
                    )
                }
            }
        }
    }
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }
}

