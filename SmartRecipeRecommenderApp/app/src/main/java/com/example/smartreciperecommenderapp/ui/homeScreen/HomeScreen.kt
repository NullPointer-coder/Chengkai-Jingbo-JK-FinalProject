package com.example.smartreciperecommenderapp.ui.homeScreen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.R
import com.example.smartreciperecommenderapp.ui.BottomNavigationBar
import com.example.smartreciperecommenderapp.ui.ErrorBanner.NetworkErrorBanner
import com.example.smartreciperecommenderapp.ui.navigation.LoginPrompt
import com.example.smartreciperecommenderapp.ui.navigation.Screen
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
        LoginPrompt(
            message = "You are not logged in yet, please log in first to use this feature.",
            onLoginClick = { navController.navigate(Screen.Account.route) }
        )
    } else {
        val recipes by homeViewModel.recipes.collectAsState()
        val selectedRecipeDetails by homeViewModel.selectedRecipeDetails.collectAsState()
        val isLoading by homeViewModel.isLoading.collectAsState()
        val errorMessage by homeViewModel.errorMessage.collectAsState()

        var shownNetworkError by remember { mutableStateOf(false) }

        // Determine if errorMessage contains "network"
        val lowerError = errorMessage?.lowercase()
        val isNetworkError = lowerError?.contains("network") == true
        val shouldShowNetworkError = isNetworkError && !shownNetworkError

        // Load initial recipes when the screen is launched
        LaunchedEffect(Unit) {
            homeViewModel.loadRecipes()
        }

        fun tryRefresh() {
            // reset shownNetworkError so if still no network, it shows again
            shownNetworkError = false
            homeViewModel.refreshRecipesLocally()
        }

        fun loadDetails(recipeId: Long) {
            // reset on new attempt
            shownNetworkError = false
            homeViewModel.loadRecipeDetails(recipeId)
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        val iconRotation = if (isLoading) rotation else 0f

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AnimatedVisibility(
                    visible = (selectedRecipeDetails == null && !isLoading && recipes.isNotEmpty()),
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
                AnimatedVisibility(
                    visible = (selectedRecipeDetails == null && !isLoading && recipes.isNotEmpty()),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { tryRefresh() },
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        modifier = Modifier.padding(16.dp)
                    ) {
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
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Use NetworkErrorBanner here
                if (shouldShowNetworkError) {
                    NetworkErrorBanner(
                        errorMessage = errorMessage,
                        durationMs = 3000L,
                        onClose = {
                            // Once closed (by user or auto), mark shownNetworkError = true
                            shownNetworkError = true
                        }
                    )
                }

                when {
                    isLoading && recipes.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    recipes.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
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
                        if (isLandscape) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                RecipeList(
                                    recipes = recipes,
                                    onRecipeClick = { recipe -> loadDetails(recipe.id) },
                                    modifier = Modifier.weight(1f)
                                )

                                RecipeDetailsPane(
                                    homeViewModel = homeViewModel,
                                    recipeDetails = selectedRecipeDetails,
                                    onBackClick = null,
                                    isLandscape = true,
                                    modifier = Modifier.weight(2f)
                                )
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (selectedRecipeDetails == null) {
                                    RecipeList(
                                        recipes = recipes,
                                        onRecipeClick = { recipe -> loadDetails(recipe.id) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    RecipeDetailsPane(
                                        homeViewModel = homeViewModel,
                                        recipeDetails = selectedRecipeDetails,
                                        onBackClick = { loadDetails(0) },
                                        isLandscape = false,
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
}
