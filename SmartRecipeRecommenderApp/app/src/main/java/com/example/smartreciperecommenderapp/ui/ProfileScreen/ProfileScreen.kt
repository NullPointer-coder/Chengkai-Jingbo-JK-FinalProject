package com.example.smartreciperecommenderapp.ui.ProfileScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.smartreciperecommenderapp.data.repository.LoginResult
import com.example.smartreciperecommenderapp.ui.ProfileScreen.Loading.LoadingScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.favoritecuisines.FavoriteCuisinesScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.loggedin.LoggedInScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.myfavorite.MyFavoriteScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.registerUsername.RegisterUsernameScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.settingsScreen.SettingsScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.signin.SignInScreen
import kotlin.math.log

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    val navController = rememberNavController() // Create NavHostController
    val loginResult by profileViewModel.loginResult.observeAsState()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            // Log the current route
            println("Current route: ${backStackEntry.destination.route}")

            // Log the routes in the navigation hierarchy
            val hierarchyRoutes = backStackEntry.destination.hierarchy.mapNotNull { it.route }
            println("Hierarchy: $hierarchyRoutes")
        }
    }



    // Set up navigation callbacks in ProfileViewModel
    profileViewModel.setNavigationHandlers(
        onMyFavorite = { navController.navigate("my_favorite") },
        onFavoriteCuisines = { navController.navigate("favorite_cuisines") },
        onSettings = { navController.navigate("settings") }
    )

    NavHost(
        navController = navController,
        startDestination = "loading" // Temporary loading screen to decide start destination
    ) {
        // Loading Screen
        composable("loading") {
            LoadingScreen(profileViewModel = profileViewModel, navController = navController)
        }

        // Sign In Screen
        composable("signin") {
            SignInScreen(
                profileViewModel = profileViewModel,
                onSignInSuccess = { navController.navigate("loggedin") }, // Navigate to logged-in screen
                onSignInFailed = {errorMessage ->
                    if (profileViewModel.loginResult.value == LoginResult.UserNotFound) {
                        profileViewModel.updateLoginResult(LoginResult.UserNotFound)
                    } else {
                        // 处理其他错误
                        Log.d("SignInScreen", "Error: $errorMessage")
                    }
                }
            )

            // Monitor login results and navigate accordingly
            LaunchedEffect(loginResult) {
                loginResult?.let {
                    print("loginResult: $loginResult")
                    when (it) {
                        is LoginResult.UserNotFound -> {
                            // Navigate to username registration screen
                            navController.navigate("registerUsername/{email}/{password}")
                        }
                        is LoginResult.Success -> {
                            // Navigate to logged-in screen
                            navController.navigate("loggedin") {
                                popUpTo("signin") { inclusive = true }
                            }
                        }
                        is LoginResult.Error -> {
                            // Handle error (e.g., display a Snackbar)
                            println(it.message)
                        }
                    }
                }
            }
        }

        // Register Username Screen
        composable("registerUsername/{email}/{password}") {
            val email by profileViewModel.temporaryEmail.observeAsState("")
            val password by profileViewModel.temporaryPassword.observeAsState("")
            var errorMessage by remember { mutableStateOf<String?>(null) }

            RegisterUsernameScreen(
                profileViewModel= profileViewModel,
                onUsernameEntered = { username ->
                    profileViewModel.registerUser(
                        email = email,
                        password = password,
                        username = username,
                        onFailure = { error ->
                            errorMessage = error // Update the error message in ProfileScreen
                        }
                    )
                    navController.navigate("signin") {
                        popUpTo("registerUsername") { inclusive = true }
                    }
                },
                navController = navController,
                onError = { error -> errorMessage = error }
            )

            errorMessage?.let {
                Toast.makeText(
                    LocalContext.current,
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Logged In Screen
        composable("loggedin") {
            LoggedInScreen(
                profileViewModel = profileViewModel,
                onMyFavoriteClick = profileViewModel.navigateToMyFavorite,
                onFavoriteCuisinesClick = profileViewModel.navigateToFavoriteCuisines,
                onSettingsClick = profileViewModel.navigateToSettings
            )
        }

        // My Favorite Screen
        composable("my_favorite") {
            MyFavoriteScreen()
        }

        // Favorite Cuisines Screen
        composable("favorite_cuisines") {
            FavoriteCuisinesScreen()
        }

        // Settings Screen
        composable("settings") {
            SettingsScreen()
        }

    }
}



