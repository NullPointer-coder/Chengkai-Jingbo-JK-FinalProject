package com.example.smartreciperecommenderapp.ui.ProfileScreen.Loading

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@Composable
fun LoadingScreen(profileViewModel: ProfileViewModel, navController: NavController) {
    // Observe login state
    val isLoggedIn by profileViewModel.isLoggedIn.observeAsState(initial = false)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // 如果已登录，导航到主界面
            navController.navigate("loggedin") {
                popUpTo("loading") { inclusive = true } // 清理导航堆栈
            }
        } else {
            // 未登录，导航到登录界面
            navController.navigate("signin") {
                popUpTo("loading") { inclusive = true }
            }
        }
    }

    // 显示加载动画
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
