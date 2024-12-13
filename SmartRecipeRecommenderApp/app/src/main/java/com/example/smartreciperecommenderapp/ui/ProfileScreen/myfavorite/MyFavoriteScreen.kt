package com.example.smartreciperecommenderapp.ui.ProfileScreen.myfavorite

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreciperecommenderapp.data.model.RecipeDetailModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.homeScreen.units.RecipeList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFavoriteScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val favorites by profileViewModel.favorites.observeAsState(emptyList())
    val favoriteDetail by profileViewModel.favoriteDetail.observeAsState(emptyList())

    var selectedDetail by remember { mutableStateOf<RecipeDetailModel?>(null) }

    // TopAppBar logic:
    // Portrait mode:
    //   - If no details selected, show topBar with title and back button.
    //   - If details are selected, no topBar.
    // Landscape mode:
    //   - You can choose to show topBar always or never. Here we choose to always show when no detail is selected.
    // Adjust this logic if needed.
    val topBar: @Composable (() -> Unit)? = if (!isLandscape && selectedDetail == null) {
        {
            TopAppBar(
                title = { Text("Favorite Cuisines") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    } else {
        // For simplicity, in landscape mode or when details are selected in portrait mode:
        // - If you want no topBar when details selected in portrait, just return null here.
        // - If you want a topBar in landscape mode, return it. Adjust as needed.
        if (!isLandscape) {
            // Portrait and details selected: no topBar
            null
        } else {
            // Landscape mode or other conditions: show topBar
            {
                TopAppBar(
                    title = { Text("Favorite Cuisines") },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = topBar ?: {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // No favorites and no selected detail
                favorites.isEmpty() && selectedDetail == null -> {
                    // Show a card indicating no favorites exist
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .wrapContentHeight()
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "No Favorites",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )

                            Text(
                                text = "No favorite cuisines yet!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Go add something you love!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = onBack,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "Find more delicious recipes",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                // Landscape mode: show split layout
                isLandscape -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left side: list of favorites
                        RecipeList(
                            recipes = favorites,
                            onRecipeClick = { recipe ->
                                val detail = favoriteDetail.firstOrNull { it.recipeId == recipe.id }
                                if (detail != null) {
                                    selectedDetail = detail
                                } else {
                                    Log.e("MyFavoriteScreen", "Detail not found for recipe: ${recipe.name}")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Right side: details pane or a placeholder if nothing selected
                        val detailModifier = Modifier.weight(2f)
                        if (selectedDetail != null) {
                            // In landscape mode, no need for a back button.
                            FavoriteRecipeDetailsPane(
                                recipeDetails = selectedDetail,
                                profileViewModel = profileViewModel,
                                onBackClick = null,
                                isLandscape = true,
                                modifier = detailModifier
                            )
                        } else {
                            // If no detail selected, show a hint
                            Box(
                                modifier = detailModifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a favorite recipe to view details.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Portrait mode
                else -> {
                    if (selectedDetail != null) {
                        // In portrait mode, if a detail is selected, show full screen detail with back button
                        FavoriteRecipeDetailsPane(
                            recipeDetails = selectedDetail,
                            profileViewModel = profileViewModel,
                            onBackClick = { selectedDetail = null },
                            isLandscape = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // No detail selected: show the list of favorites
                        RecipeList(
                            recipes = favorites,
                            onRecipeClick = { recipe ->
                                val detail = favoriteDetail.firstOrNull { it.recipeId == recipe.id }
                                if (detail != null) {
                                    selectedDetail = detail
                                } else {
                                    Log.e("MyFavoriteScreen", "Detail not found for recipe: ${recipe.name}")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
