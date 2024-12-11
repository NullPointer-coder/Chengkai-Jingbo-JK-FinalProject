package com.example.smartreciperecommenderapp.ui.homeScreen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartreciperecommenderapp.ui.BottomNavigationBar
import com.example.smartreciperecommenderapp.ui.api.FatSecretRecipeDetailsResponse
import com.example.smartreciperecommenderapp.ui.api.Recipe
import com.example.smartreciperecommenderapp.ui.navigation.LoginPrompt
import com.example.smartreciperecommenderapp.ui.navigation.Screen

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

        LaunchedEffect(Unit) {
            homeViewModel.loadRecipes()
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Recommended Recipes", style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { innerPadding ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                recipes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recipes found. Please add ingredients.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    if (isLandscape) {
                        // Multi-pane layout in landscape mode
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            RecipeList(
                                recipes = recipes,
                                onRecipeClick = { recipe ->
                                    homeViewModel.loadRecipeDetails(recipe.recipe_id.toLong())
                                },
                                modifier = Modifier.weight(1f)
                            )
                            RecipeDetailsPane(
                                recipeDetails = selectedRecipeDetails,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Single-pane layout in portrait mode
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (selectedRecipeDetails == null) {
                                RecipeList(
                                    recipes = recipes,
                                    onRecipeClick = { recipe ->
                                        homeViewModel.loadRecipeDetails(recipe.recipe_id.toLong())
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                RecipeDetailsPane(
                                    recipeDetails = selectedRecipeDetails,
                                    onBackClick = { homeViewModel.loadRecipeDetails(0) }, // Clear details
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

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recipes) { recipe ->
            RecipeItemCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) }
            )
        }
    }
}

@Composable
fun RecipeItemCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = recipe.recipe_image),
                contentDescription = recipe.recipe_name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = recipe.recipe_name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = recipe.recipe_description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun RecipeDetailsPane(
    recipeDetails: FatSecretRecipeDetailsResponse?,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    if (recipeDetails != null) {
        Column(modifier = modifier.fillMaxSize()) {
            if (onBackClick != null) {
                IconButton(
                    onClick = { onBackClick() },
                    modifier = Modifier.padding(bottom = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Recipe Name and Description
                item {
                    Text(
                        text = recipeDetails.recipe_name,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Description: ${recipeDetails.recipe_description}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Number of Servings and Portion Size
                item {
                    Text(
                        text = "Servings: ${recipeDetails.number_of_servings}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recipeDetails.grams_per_portion?.let { gramsPerPortion ->
                        Text(
                            text = "Grams per Portion: $gramsPerPortion",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Preparation and Cooking Time
                item {
                    recipeDetails.preparation_time_min?.let { prepTime ->
                        Text(
                            text = "Preparation Time: $prepTime minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    recipeDetails.cooking_time_min?.let { cookTime ->
                        Text(
                            text = "Cooking Time: $cookTime minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Recipe Types
                item {
                    recipeDetails.recipe_types?.recipe_type?.let { recipeTypes ->
                        Text(
                            text = "Recipe Types: ${recipeTypes.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Recipe Categories
                item {
                    recipeDetails.recipe_categories?.recipe_category?.let { recipeCategories ->
                        Text(
                            text = "Categories: ${recipeCategories.joinToString(", ") { it.recipe_category_name }}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Recipe Images
                item {
                    recipeDetails.recipe_images?.recipe_image?.let { images ->
                        Text(
                            text = "Images:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        images.forEach { imageUrl ->
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUrl),
                                contentDescription = "Recipe Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .padding(bottom = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Ingredients
                item {
                    Text(
                        text = "Ingredients:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recipeDetails.ingredients.ingredient.forEach { ingredient ->
                        Text(
                            text = "- ${ingredient.ingredient_description}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                        )
                    }
                }

                // Directions
                item {
                    Text(
                        text = "Directions:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recipeDetails.directions.direction.forEach { direction ->
                        Text(
                            text = "${direction.direction_number}. ${direction.direction_description}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a recipe to view details.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

