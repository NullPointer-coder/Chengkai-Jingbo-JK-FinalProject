package com.example.smartreciperecommenderapp.ui.homeScreen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.BottomNavigationBar
import com.example.smartreciperecommenderapp.ui.navigation.*
import com.example.smartreciperecommenderapp.R
import com.example.smartreciperecommenderapp.ui.homeScreen.units.RecipeDetailsPane
import com.example.smartreciperecommenderapp.ui.homeScreen.units.RecipeList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    homeViewModel: HomeViewModel = viewModel()
) {
    if (!isLoggedIn) {
        // If the user is not logged in, show a prompt to log in first
        LoginPrompt(
            message = "You are not logged in yet, please log in first to use this feature.",
            onLoginClick = { navController.navigate(Screen.Account.route) }
        )
    } else {
        val recipes by homeViewModel.recipes.collectAsState()
        val selectedRecipeDetails by homeViewModel.selectedRecipeDetails.collectAsState()
        val isLoading by homeViewModel.isLoading.collectAsState()

        // Load initial recipes when the screen is launched
        LaunchedEffect(Unit) {
            homeViewModel.loadRecipes()
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d("HomeScreen", "isLandscape: ${recipes.isEmpty()}")

        // Rotation animation for the sync icon when loading
        val infiniteTransition = rememberInfiniteTransition(label = "sync reload")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sync reload"
        )

        // If loading, rotate the icon; if not, show it still
        val iconRotation = if (isLoading) rotation else 0f

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // TopAppBar is only visible when no recipe details are selected
                AnimatedVisibility(
                    visible = (selectedRecipeDetails == null),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                "Recommended Recipes",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            },
            bottomBar = {
                BottomNavigationBar(navController)
            },
            floatingActionButton = {
                // Show the FAB only when no recipe details are being shown
                AnimatedVisibility(
                    visible = (selectedRecipeDetails == null && !isLoading && recipes.isNotEmpty()),
                    enter = fadeIn(),
                    exit = fadeOut()
                ){
                    FloatingActionButton(
                        onClick = {
                            // Refresh recipes
                            homeViewModel.refreshRecipesLocally()
                        },
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Sync icon that rotates if loading
                        Image(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "Refresh",
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    rotationZ = iconRotation
                                }
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Main content area
            when {
                // Show loading indicator if no data and isLoading is true
                isLoading && recipes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // If no recipes are found, prompt user to add ingredients
                recipes.isEmpty() -> {
                    Log.d("HomeScreen", "No recipes found")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Text(
                            text = "No recipes found. Please add ingredients.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(onClick = { navController.navigate(Screen.Ingredient.route) }) {
                            Text("Add Ingredients")
                        }
                    }
                }
                else -> {
                    // If in landscape mode, use a two-pane layout
                    if (isLandscape) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            RecipeList(
                                recipes = recipes,
                                onRecipeClick = { recipe ->
                                    homeViewModel.loadRecipeDetails(recipe.id)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            RecipeDetailsPane(
                                homeViewModel = homeViewModel,
                                recipeDetails = selectedRecipeDetails,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // In portrait mode, use a single-pane layout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (selectedRecipeDetails == null) {
                                // Show recipe list
                                RecipeList(
                                    recipes = recipes,
                                    onRecipeClick = { recipe ->
                                        homeViewModel.loadRecipeDetails(recipe.id)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Show selected recipe details
                                RecipeDetailsPane(
                                    homeViewModel = homeViewModel,
                                    recipeDetails = selectedRecipeDetails,
                                    onBackClick = { homeViewModel.loadRecipeDetails(0) }, // Back button to clear details
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
