package com.example.smartreciperecommenderapp.ui.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import androidx.compose.ui.unit.dp

import com.example.smartreciperecommenderapp.data.repository.IngredientRepository
import com.example.smartreciperecommenderapp.data.repository.LoginResult
import com.example.smartreciperecommenderapp.ui.IngredientScreen.IngredientViewModel
import com.example.smartreciperecommenderapp.ui.IngredientScreen.IngredientViewModelFactory
import com.example.smartreciperecommenderapp.ui.IngredientScreen.barcodeResult.ProductDetailScreen
import com.example.smartreciperecommenderapp.ui.IngredientScreen.camera.QRScannerScreen
import com.example.smartreciperecommenderapp.ui.IngredientScreen.camera.QRScannerViewModel
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
    object RecipeDetail : Screen("recipe_detail", Icons.Filled.Home)
    object BarcodeScanner : Screen("barcode_scanner", Icons.Filled.QrCodeScanner)
    object SignIn : Screen("sign_in", Icons.Filled.PersonOutline)
    object LoggedIn : Screen("logged_in", Icons.Filled.PersonOutline)
    object Settings : Screen("settings", Icons.Filled.PersonOutline)
    object MyFavorite : Screen("my_favorite", Icons.Filled.PersonOutline)
    object FavoriteCuisines : Screen("favorite_cuisines", Icons.Filled.PersonOutline)
    object ProductDetail : Screen("product_detail", Icons.Filled.Restaurant)
}


@Composable
fun NavGraph(
    navController: NavHostController,
    profileViewModel: ProfileViewModel,
    ingredientRepository: IngredientRepository
) {
    val qrScannerViewModel: QRScannerViewModel = viewModel()
    val loginResult by profileViewModel.loginResult.observeAsState()
    val isEmailVerified by profileViewModel.isEmailVerified.observeAsState(false)
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val displayedMessages = remember { mutableSetOf<String>() }

    // 是否已经导航过到LoggedIn和SignIn的标记
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

    // 动态处理登录结果
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

    // 邮箱验证状态
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

    val isLoggedIn by profileViewModel.isLoggedIn.observeAsState(false)

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
                        },
                        onFailure = { error -> errorMessage = error }
                    )
                    navController.navigate(Screen.Account.route) {
                        popUpTo(ProfileRoutes.REGISTER_USERNAME) { inclusive = true }
                    }
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
                onEditAccount = { /* Navigate to edit account screen if needed */ },
                onLogout = {
                    profileViewModel.logout()
                    navController.navigate(Screen.Account.route) {
                        popUpTo(Screen.Account.route) { inclusive = true }
                    }
                },
                onResetNavigatedToLoggedIn = { hasNavigatedToLoggedIn = false }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                isLoggedIn = isLoggedIn
            )
        }

        composable(Screen.Ingredient.route) {
            val ingredientViewModel: IngredientViewModel = viewModel(
                factory = IngredientViewModelFactory(ingredientRepository)
            )
            IngredientScreen(
                navController = navController,
                isLoggedIn = isLoggedIn,
                qrScannerViewModel = qrScannerViewModel,
                ingredientViewModel = ingredientViewModel
            )
        }

        composable(Screen.Account.route) {
            val currentIsLoggedIn by profileViewModel.isLoggedIn.observeAsState(false)

            // 如果登录了，尝试获取用户信息
            LaunchedEffect(currentIsLoggedIn) {
                if (currentIsLoggedIn) {
                    profileViewModel.fetchUserDetails()
                }
            }

            if (currentIsLoggedIn) {
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

        composable(Screen.BarcodeScanner.route) {
            QRScannerScreen(navController = navController, viewModel = qrScannerViewModel)
        }

        composable(Screen.ProductDetail.route) {
            Log.d("ProductDetailScreen", "Navigating to ProductDetailScreen")
            val ingredientViewModel: IngredientViewModel = viewModel(
                factory = IngredientViewModelFactory(ingredientRepository)
            )
            ProductDetailScreen(
                navController = navController,
                qRScannerViewModel = qrScannerViewModel,
                ingredientViewModel= ingredientViewModel
            )
        }
    }
}

@Composable
fun LoginPrompt(
    message: String,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        ElevatedCard(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = androidx.compose.ui.Modifier.padding(bottom = 16.dp)
                )
                FilledTonalButton(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(text = "Go to Sign In")
                }
            }
        }
    }
}
