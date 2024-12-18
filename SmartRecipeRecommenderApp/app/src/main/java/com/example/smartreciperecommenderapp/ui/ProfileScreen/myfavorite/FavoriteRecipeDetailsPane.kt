package com.example.smartreciperecommenderapp.ui.ProfileScreen.myfavorite

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.smartreciperecommenderapp.data.model.RecipeDetailModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.homeScreen.units.DirectionsWithFullScreenOverlay
import com.example.smartreciperecommenderapp.ui.homeScreen.units.IngredientsSection
import com.example.smartreciperecommenderapp.ui.homeScreen.units.MacrosDonutChart
import com.example.smartreciperecommenderapp.ui.homeScreen.units.NutritionFactsLabel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FavoriteRecipeDetailsPane(
    recipeDetails: RecipeDetailModel?,
    profileViewModel: ProfileViewModel,
    onBackClick: (() -> Unit)? = null,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
// If no recipe details are available, show a hint
    if (recipeDetails == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a favorite recipe to view details.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var isFavorited by remember { mutableStateOf(false) }

    LaunchedEffect(recipeDetails.recipeId) {
        val result = profileViewModel.isRecipeFavorite(recipeDetails.recipeId)
        isFavorited = result
    }

    val scrollState = rememberLazyListState()
    val mainImageUrl = recipeDetails.imageUrl

    // Fetch parsed data from ViewModel
    val categories = profileViewModel.getCategories(recipeDetails)
    val types = profileViewModel.getTypes(recipeDetails)
    val servingSizes = profileViewModel.getServingSizes(recipeDetails)
    val ingredients = profileViewModel.getIngredients(recipeDetails)
    val directions = profileViewModel.getDirections(recipeDetails)

    Scaffold(
        modifier = modifier,
        topBar = {
            // Top bar with recipe name and optional back button
            TopAppBar(
                title = {
                    Text(
                        text = recipeDetails.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    if (!isLandscape && onBackClick != null) {
                        IconButton(onClick = { onBackClick() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isFavorited) {
                            // If currently favorited, remove it from favorites
                            profileViewModel.removeRecipeFromFavorite(recipeDetails)
                            isFavorited = false
                        } else {
                            // If not favorited, add it to favorites
                            profileViewModel.saveMyFavoriteRecipe(recipeDetails)
                            isFavorited = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorited) Color.Red else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main image
                item {
                    if (mainImageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = mainImageUrl),
                            contentDescription = recipeDetails.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Description section
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (recipeDetails.description.isNotEmpty()) {
                            Text(
                                text = recipeDetails.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Recipe info card with icons and summary
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.elevatedCardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Recipe Info",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            val iconTint = MaterialTheme.colorScheme.onSurface
                            val iconSize = 24.dp // slightly bigger icon size
                            val textStyle = MaterialTheme.typography.bodyMedium

                            // A helper composable to standardize the row layout of icon + text
                            @Composable
                            fun InfoRow(icon: @Composable () -> Unit, text: String) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp) // vertical spacing between items
                                ) {
                                    Box(modifier = Modifier.size(iconSize)) {
                                        icon()
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = text,
                                        style = textStyle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Servings
                            InfoRow(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = "Servings",
                                        tint = iconTint
                                    )
                                },
                                text = "Servings: ${recipeDetails.servings.toInt()}"
                            )

                            recipeDetails.gramsPerPortion?.let { gramsPerPortion ->
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                InfoRow(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Scale,
                                            contentDescription = "Grams per portion",
                                            tint = iconTint
                                        )
                                    },
                                    text = "Grams/Portion: ${gramsPerPortion.toInt()}g"
                                )
                            }

                            recipeDetails.prepTime?.let { prepTime ->
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                InfoRow(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Prep Time",
                                            tint = iconTint
                                        )
                                    },
                                    text = "Prep Time: $prepTime min"
                                )
                            }

                            recipeDetails.cookTime?.let { cookTime ->
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                InfoRow(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Fireplace,
                                            contentDescription = "Cook Time",
                                            tint = iconTint
                                        )
                                    },
                                    text = "Cook Time: $cookTime min"
                                )
                            }

                            val mealTypes = types?.recipe_type
                            if (!mealTypes.isNullOrEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                val mealTypesString = mealTypes.joinToString(", ")
                                InfoRow(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.RestaurantMenu,
                                            contentDescription = "Meal Types",
                                            tint = iconTint
                                        )
                                    },
                                    text = "Meal Types: $mealTypesString"
                                )
                            }
                        }
                    }

                }

                // Tags displayed as chips
                // In your code where tags are displayed:
                if ((categories?.recipe_category?.isNotEmpty() == true)) {
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tags",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Add a divider for better separation
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                // Use FlowRow to wrap chips nicely with spacing
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Define chip colors once to keep consistent styling
                                    val chipColors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    categories.recipe_category.forEach { category ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(text = category.recipe_category_name) },
                                            colors = chipColors,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // Ingredients list with check icons
                if (ingredients.ingredient.isNotEmpty()) {
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                IngredientsSection(ingredients)
                            }
                        }
                    }
                }

                // Nutritional Information with NutritionFactsLabel and MacrosDonutChart
                servingSizes?.serving?.let { s ->
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Per Serving",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Show the nutrition facts label
                                Box(modifier = Modifier.padding(16.dp)) {
                                    NutritionFactsLabel(s)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Show the donut chart of macros
                                Box(modifier = Modifier.padding(16.dp)) {
                                    MacrosDonutChart(
                                        s.calories.toInt(),
                                        s.fat,
                                        s.carbohydrate,
                                        s.protein
                                    )
                                }
                            }
                        }
                    }
                }

                // Directions section with step indicators
                if (directions.direction.isNotEmpty()) {
                    item {
                        DirectionsWithFullScreenOverlay(directions)
                    }
                }

                // Add some spacing at the bottom
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    )
}
