package com.example.smartreciperecommenderapp.ui.ProfileScreen.Loading

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@Composable
fun LoadingScreen(profileViewModel: ProfileViewModel, navController: NavController) {
    // Observe login state
    val isLoggedIn by profileViewModel.isLoggedIn.observeAsState(initial = false)

    // Navigate to the appropriate screen
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("loggedin") {
                popUpTo("loading") { inclusive = true } // Clear the back stack
            }
        } else {
            navController.navigate("signin") {
                popUpTo("loading") { inclusive = true } // Clear the back stack
            }
        }
    }

    // Display a loading spinner while deciding
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
