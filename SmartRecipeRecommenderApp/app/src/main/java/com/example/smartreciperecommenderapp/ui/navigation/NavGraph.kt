package com.example.smartreciperecommenderapp.ui.navigation

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartreciperecommenderapp.data.repository.UserRepository
//import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModelFactory

import com.example.smartreciperecommenderapp.ui.*
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.IngredientScreen.IngredientScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.signin.SignInScreen
import com.example.smartreciperecommenderapp.ui.homeScreen.HomeScreen

import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.smartreciperecommenderapp.data.repository.LoginResult
import com.example.smartreciperecommenderapp.ui.ProfileScreen.Loading.LoadingScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileRoutes
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.favoritecuisines.FavoriteCuisinesScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.loggedin.LoggedInScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.myfavorite.MyFavoriteScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.registerUsername.RegisterUsernameScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.settingsScreen.SettingsScreen


sealed class Screen(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("Home", Icons.Filled.Home)
    object Account : Screen("Account", Icons.Filled.PersonOutline)
    object Ingredient : Screen("Ingredient", Icons.Filled.ShoppingCart)
    object RecipeDetail : Screen("recipe_detail", Icons.Filled.Home) // Example
    object BarcodeScanner : Screen("barcode_scanner", Icons.Filled.Home) // Example
    object SignIn : Screen("sign_in", Icons.Filled.PersonOutline) // Example
    object LoggedIn : Screen("logged_in", Icons.Filled.PersonOutline)
    object Settings : Screen("settings", Icons.Filled.PersonOutline)
    object MyFavorite : Screen("my_favorite", Icons.Filled.PersonOutline)
    object FavoriteCuisines : Screen("favorite_cuisines", Icons.Filled.PersonOutline)

}

@Composable
fun NavGraph(navController: NavHostController, profileViewModel: ProfileViewModel) {

    /*
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(UserRepository())
    )
     */

    val loginResult by profileViewModel.loginResult.observeAsState()
    val isEmailVerified by profileViewModel.isEmailVerified.observeAsState(false)
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val displayedMessages = remember { mutableSetOf<String>() }

    // 用于记录导航状态，防止重复导航
    var hasNavigatedToLoggedIn by remember { mutableStateOf(false) }
    var hasNavigatedToSignIn by remember { mutableStateOf(false) }

    // 处理错误信息显示
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (!displayedMessages.contains(it)) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                displayedMessages.add(it)
            }
        }
    }

    // Handle login results dynamically
    LaunchedEffect(loginResult) {
        when (loginResult) {
            is LoginResult.Success -> {
                profileViewModel.checkEmailVerification()
                profileViewModel.resetLoginResult()
            }
            is LoginResult.Error -> {
                errorMessage = (loginResult as LoginResult.Error).errorMessage
                profileViewModel.resetLoginResult()
            }
            LoginResult.UserNotFound -> {
                if (!hasNavigatedToSignIn) {
                    hasNavigatedToSignIn = true
                    errorMessage = "User not found. Redirecting to registration."
                    navController.navigate(ProfileRoutes.REGISTER_USERNAME) {
                        popUpTo(ProfileRoutes.SIGN_IN) { inclusive = true }
                    }
                }
                profileViewModel.resetLoginResult()
            }
            null -> Unit
        }
    }


    // 处理邮箱验证状态
    LaunchedEffect(isEmailVerified) {
        if (isEmailVerified && !hasNavigatedToLoggedIn) {
            hasNavigatedToLoggedIn = true
            profileViewModel.fetchUserDetails {
                navController.navigate(ProfileRoutes.LOGGED_IN) {
                    popUpTo(ProfileRoutes.SIGN_IN) { inclusive = true }
                }
            }
        }
    }

    // 设置导航回调
    profileViewModel.setNavigationHandlers(
        onMyFavorite = { navController.navigate(ProfileRoutes.MY_FAVORITE) },
        onFavoriteCuisines = { navController.navigate(ProfileRoutes.FAVORITE_CUISINES) },
        onSettings = { navController.navigate(ProfileRoutes.SETTINGS) }
    )



    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {


        composable(ProfileRoutes.REGISTER_USERNAME) {
            RegisterUsernameScreen(
                profileViewModel = profileViewModel,
                onUsernameEntered = { username ->
                    profileViewModel.registerUser(
                        email = profileViewModel.temporaryEmail.value ?: "",
                        password = profileViewModel.temporaryPassword.value ?: "",
                        username = username,
                        onEmailVerificationPending = {
                            errorMessage = "Check your email for verification."
                            navController.navigate(ProfileRoutes.SIGN_IN) {
                                popUpTo(ProfileRoutes.REGISTER_USERNAME) { inclusive = false }
                            }
                        },
                        onFailure = { error -> errorMessage = error }
                    )
                    // 123
                },
                navController = navController,
                onError = { error ->
                    errorMessage = error
                },
                onResetNavigatedToSignIn = { hasNavigatedToSignIn = false }
            )
        }



        composable(ProfileRoutes.MY_FAVORITE) {
            MyFavoriteScreen(
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(ProfileRoutes.FAVORITE_CUISINES) {
            FavoriteCuisinesScreen(
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(ProfileRoutes.SETTINGS) {
            SettingsScreen(
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() },
                onEditAccount = { /* Navigate to edit account screen */ },
                onLogout = {
                    profileViewModel.logout()
                    navController.navigate(Screen.Account.route) {
                        popUpTo(Screen.Account.route) { inclusive = true }
                    }
                },
                onResetNavigatedToLoggedIn = { hasNavigatedToLoggedIn = false }
            )
        }

        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Ingredient.route) { IngredientScreen(navController) }
        // composable(Screen.Account.route) { LoadingScreen(profileViewModel = profileViewModel, navController = navController)}

        // Account Screen (SignIn or Profile)
        composable(Screen.Account.route) {
            val isLoggedIn by profileViewModel.isLoggedIn.observeAsState(false)

            // Fetch user details when logged in
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    profileViewModel.fetchUserDetails()
                }
            }

            if (isLoggedIn) {
                LoggedInScreen(
                    profileViewModel = profileViewModel,
                    onMyFavoriteClick = { navController.navigate(ProfileRoutes.MY_FAVORITE) },
                    onFavoriteCuisinesClick = { navController.navigate(ProfileRoutes.FAVORITE_CUISINES) },
                    onSettingsClick = { navController.navigate(ProfileRoutes.SETTINGS) }
                )
            } else {
                SignInScreen(
                    profileViewModel = profileViewModel,
                    onSignInSuccess = {
                        navController.navigate(Screen.Account.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onSignInFailed = { error -> errorMessage = error }
                )
            }
        }

        // composable(Screen.RecipeDetail.route) { RecipeDetailScreen(navController) }
        // composable(Screen.BarcodeScanner.route) { BarcodeScannerScreen(navController) }
    }
}

