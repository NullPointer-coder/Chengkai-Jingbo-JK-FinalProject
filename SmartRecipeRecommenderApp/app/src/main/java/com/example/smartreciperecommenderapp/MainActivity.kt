package com.example.smartreciperecommenderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.smartreciperecommenderapp.data.model.AppDatabase
import com.example.smartreciperecommenderapp.data.repository.FirebaseIngredientService
import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.ui.BottomNavigationBar
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModelFactory
import com.example.smartreciperecommenderapp.ui.navigation.NavGraph
import com.example.smartreciperecommenderapp.ui.theme.SmartRecipeRecommenderAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var ingredientRepository: IngredientRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationContext.deleteDatabase("smart_recipe_db")
        // Initialize the database and dependencies in onCreate// 在onCreate中初始化数据库和依赖
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smart_recipe_db"
        ).build()

        val firebaseIngredientService = FirebaseIngredientService()
        val ingredientDao = database.ingredientDao()
        ingredientRepository = IngredientRepository(ingredientDao, firebaseIngredientService)

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
                        ingredientRepository = ingredientRepository
                    )
                }
            }
        }
    }
}
