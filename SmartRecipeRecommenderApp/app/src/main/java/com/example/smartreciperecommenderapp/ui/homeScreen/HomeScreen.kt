package com.example.smartreciperecommenderapp.ui.homeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.navigation.LoginPrompt
import com.example.smartreciperecommenderapp.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, isLoggedIn: Boolean) {
    if (!isLoggedIn) {
        LoginPrompt(
            message = "You are not logged in yet, please log in first to use this feature.",
            onLoginClick = { navController.navigate(Screen.Account.route) }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Recipes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Dummy list of recipes
                items(10) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                // Navigate to the RecipeDetail screen
                                navController.navigate(Screen.RecipeDetail.route)
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Recipe $index",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Click to see details",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
