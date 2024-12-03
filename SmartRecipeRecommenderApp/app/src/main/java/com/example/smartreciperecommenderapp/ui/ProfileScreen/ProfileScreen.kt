package com.example.smartreciperecommenderapp.ui.ProfileScreen

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                },
                onResetNavigatedToSignIn = { hasNavigatedToSignIn = false }
            )
        }

        composable(ProfileRoutes.LOGGED_IN) {
            if (isEmailVerified) {
                LoggedInScreen(
                    profileViewModel = profileViewModel,
                    onMyFavoriteClick = profileViewModel.navigateToMyFavorite,
                    onFavoriteCuisinesClick = profileViewModel.navigateToFavoriteCuisines,
                    onSettingsClick = profileViewModel.navigateToSettings
                )
            } else {
                if (!hasNavigatedToSignIn) {
                    hasNavigatedToSignIn = true
                    errorMessage = "Access denied. Please verify your email first."
                    navController.navigate(ProfileRoutes.SIGN_IN) {
                        popUpTo(ProfileRoutes.LOGGED_IN) { inclusive = true }
                    }
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
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() },
                onEditAccount = { /* Navigate to edit account screen */ },
                onLogout = {
                    profileViewModel.logout()
                    navController.navigate(ProfileRoutes.SIGN_IN) {
                        popUpTo(ProfileRoutes.SETTINGS) { inclusive = true }
                    }
                },
                onResetNavigatedToLoggedIn = { hasNavigatedToLoggedIn = false }
            )
        }
    }
}