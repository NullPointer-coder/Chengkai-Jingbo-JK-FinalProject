package com.example.smartreciperecommenderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.ui.BottomNavigationBar
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModelFactory
import com.example.smartreciperecommenderapp.ui.navigation.NavGraph
import com.example.smartreciperecommenderapp.ui.theme.SmartRecipeRecommenderAppTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        profileViewModel = profileViewModel
                    )


                        /*
                         val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController) } // 添加底部导航栏
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.HOME // 设置默认页面
                    ) {
                        composable(NavRoutes.HOME) {
                            HomeScreen(navController)
                        }

                        composable(NavRoutes.INGREDIENTS) {
                            IngredientScreen(navController)
                        }

                        composable(NavRoutes.PROFILE) {
                            ProfileScreen(
                                profileViewModel = viewModel(factory = ProfileViewModelFactory(userRepository)),
                                navController = navController
                            )
                        * */

                }
            }
        }
    }

}
