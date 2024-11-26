package com.example.smartreciperecommenderapp.ui.ProfileScreen

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
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
fun ProfileScreen(profileViewModel: ProfileViewModel, navController: NavHostController) {
    val loginResult by profileViewModel.loginResult.observeAsState()
    val isEmailVerified by profileViewModel.isEmailVerified.observeAsState(false)


    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            println("Current route: ${backStackEntry.destination.route}")
        }
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        errorMessage = null
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
                navController.navigate(ProfileRoutes.REGISTER_USERNAME) {
                    popUpTo(ProfileRoutes.SIGN_IN) { inclusive = true }
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

    // 导航主机
    NavHost(
        navController = navController,
        startDestination = ProfileRoutes.LOADING
    ) {
        composable(ProfileRoutes.LOADING) {
            LoadingScreen(profileViewModel = profileViewModel, navController = navController)
        }

        composable(ProfileRoutes.SIGN_IN) {
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

        composable(ProfileRoutes.REGISTER_USERNAME) { backStackEntry ->
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
                    navController.navigate(ProfileRoutes.SIGN_IN) {
                        popUpTo("registerUsername") { inclusive = true }
                    }
                },
                navController = navController,
                onError = { error ->
                    errorMessage = error
                }
            )
        }

        composable(ProfileRoutes .LOGGED_IN) {
            if (isEmailVerified) {
                LoggedInScreen(
                    profileViewModel = profileViewModel,
                    onMyFavoriteClick = profileViewModel.navigateToMyFavorite,
                    onFavoriteCuisinesClick = profileViewModel.navigateToFavoriteCuisines,
                    onSettingsClick = profileViewModel.navigateToSettings
                )
            } else {
                errorMessage = "Access denied. Please verify your email first."
                navController.navigate(ProfileRoutes.SIGN_IN) {
                    popUpTo(ProfileRoutes.LOGGED_IN) { inclusive = true }
                }
            }
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
                profileViewModel = profileViewModel, // 传递 ViewModel
                onBack = { navController.popBackStack() }, // 返回上一页
                onEditAccount = { /* Navigate to edit account screen */ },
                onLogout = {
                    profileViewModel.logout()
                    navController.navigate(ProfileRoutes.SIGN_IN) { // 返回登录页面
                        popUpTo(ProfileRoutes.SETTINGS) { inclusive = true }
                    }
                }
            )
        }

    }
}
