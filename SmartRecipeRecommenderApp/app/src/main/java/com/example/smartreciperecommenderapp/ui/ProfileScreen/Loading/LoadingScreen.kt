package com.example.smartreciperecommenderapp.ui.ProfileScreen.Loading

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

/**
 * A loading screen that waits for the user's login state to be determined.
 * Once the login state is known, navigates to the appropriate screen.
 */
@Composable
fun LoadingScreen(profileViewModel: ProfileViewModel, navController: NavController) {
    // Observe the user's login state from the ProfileViewModel
    val isLoggedIn by profileViewModel.isLoggedIn.observeAsState(initial = false)

    // When the login state changes, navigate accordingly
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // If the user is logged in, navigate to the main (logged-in) screen
            navController.navigate("loggedin") {
                popUpTo("loading") { inclusive = true } // Clear the navigation stack
            }
        } else {
            // If the user is not logged in, navigate to the sign-in screen
            navController.navigate("signin") {
                popUpTo("loading") { inclusive = true }
            }
        }
    }

    // Show a loading indicator while determining the user's login state
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
