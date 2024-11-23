package com.example.smartreciperecommenderapp.ui.ProfileScreen

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
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


@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    val navController = rememberNavController() // 创建 NavController
    val loginResult by profileViewModel.loginResult.observeAsState()
    val email by profileViewModel.temporaryEmail.observeAsState("")
    val password by profileViewModel.temporaryPassword.observeAsState("")
    val isEmailVerified by profileViewModel.isEmailVerified.observeAsState(false)

    // 状态管理
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 打印导航信息用于调试
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            println("Current route: ${backStackEntry.destination.route}")
        }
    }

    // 处理登录结果变化
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
                errorMessage = "User not found. Redirecting to registration."
                navController.navigate("registerUsername/$email/$password") {
                    popUpTo("signin") { inclusive = true }
                }
                profileViewModel.resetLoginResult()
            }
            null -> {}
        }
    }

    // 显示错误消息
    val context = LocalContext.current
    val displayedMessages = remember { mutableSetOf<String>() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (!displayedMessages.contains(it)) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                displayedMessages.add(it) // 避免重复显示同样的消息
            }
        }
    }

    LaunchedEffect(isEmailVerified) {
        if (isEmailVerified) {
            navController.navigate("loggedin") {
                popUpTo("signin") { inclusive = true }
            }
        }
    }

    // 设置导航回调
    profileViewModel.setNavigationHandlers(
        onMyFavorite = { navController.navigate("my_favorite") },
        onFavoriteCuisines = { navController.navigate("favorite_cuisines") },
        onSettings = { navController.navigate("settings") }
    )

    // 导航主机
    NavHost(
        navController = navController,
        startDestination = "loading"
    ) {
        composable("loading") {
            LoadingScreen(profileViewModel = profileViewModel, navController = navController)
        }

        composable("signin") {
            SignInScreen(
                profileViewModel = profileViewModel,
                onSignInSuccess = {
                    profileViewModel.checkEmailVerification()
                },
                onSignInFailed = { error ->
                    errorMessage = error
                }
            )
        }

        composable("registerUsername/{email}/{password}") { backStackEntry ->
            val emailArg = backStackEntry.arguments?.getString("email") ?: ""
            val passwordArg = backStackEntry.arguments?.getString("password") ?: ""

            RegisterUsernameScreen(
                profileViewModel = profileViewModel,
                onUsernameEntered = { username ->
                    profileViewModel.registerUser(
                        email = emailArg,
                        password = passwordArg,
                        username = username,
                        onEmailVerificationPending = {
                            errorMessage = "Check your email for verification."
                        },
                        onFailure = { error ->
                            errorMessage = error
                        }
                    )
                    navController.navigate("signin") {
                        popUpTo("registerUsername") { inclusive = true }
                    }
                },
                navController = navController,
                onError = { error ->
                    errorMessage = error
                }
            )
        }

        composable("loggedin") {
            if (isEmailVerified) {
                LoggedInScreen(
                    profileViewModel = profileViewModel,
                    onMyFavoriteClick = profileViewModel.navigateToMyFavorite,
                    onFavoriteCuisinesClick = profileViewModel.navigateToFavoriteCuisines,
                    onSettingsClick = profileViewModel.navigateToSettings
                )
            } else {
                errorMessage = "Access denied. Please verify your email first."
                navController.navigate("signin") {
                    popUpTo("loggedin") { inclusive = true }
                }
            }
        }

        composable("my_favorite") {
            MyFavoriteScreen()
        }

        composable("favorite_cuisines") {
            FavoriteCuisinesScreen()
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}
