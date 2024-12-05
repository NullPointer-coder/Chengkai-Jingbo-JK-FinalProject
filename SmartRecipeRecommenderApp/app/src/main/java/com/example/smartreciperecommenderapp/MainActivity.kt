package com.example.smartreciperecommenderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.smartreciperecommenderapp.data.model.*
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.data.repository.fetchAndStoreCategories
import com.example.smartreciperecommenderapp.ui.BottomNavigationBar
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModelFactory
import com.example.smartreciperecommenderapp.ui.api.RetrofitInstance
import com.example.smartreciperecommenderapp.ui.navigation.NavGraph
import com.example.smartreciperecommenderapp.ui.theme.SmartRecipeRecommenderAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smart_recipe_db"
        ).build()

        val categoriesViewModel = CategoriesViewModel(database.categoryDao())

        val userRepository = UserRepository()
        val profileViewModel: ProfileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(userRepository)
        )[ProfileViewModel::class.java]

        enableEdgeToEdge()

        lifecycleScope.launch(Dispatchers.IO) {
            val categories = categoriesViewModel.categories.value
            if (categories.isEmpty()) {
                fetchAndStoreCategories(RetrofitInstance.api, categoriesViewModel)
            }
        }

        setContent {
            SmartRecipeRecommenderAppTheme {
                val navController = rememberNavController()

                val categories = categoriesViewModel.categories.collectAsState().value

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        profileViewModel = profileViewModel,
                        categories = categories
                    )
                }
            }
        }
    }
}
