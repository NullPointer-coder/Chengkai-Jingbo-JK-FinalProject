package com.example.smartreciperecommenderapp.ui.ProfileScreen.myfavorite

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.smartreciperecommenderapp.data.model.RecipeDetailModel

import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.homeScreen.units.RecipeList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFavoriteScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val favorites by profileViewModel.favorites.observeAsState(emptyList())
    val favoriteDetail by profileViewModel.favoriteDetail.observeAsState(emptyList())

    var selectedDetail by remember { mutableStateOf<RecipeDetailModel?>(null) }

    // Conditionally set the topBar based on selectedDetail
    val topBar: @Composable (() -> Unit)? = if (selectedDetail == null) {
        {
            TopAppBar(
                title = { Text("Favorite Cuisines") },
                navigationIcon = {
                    IconButton(onClick = {
                        // If we are viewing details, go back to the list
                        // Otherwise, go back as usual
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    } else {
        null // Hide the top bar when displaying details
    }

    // Always use Scaffold, even if topBar is null
    Scaffold(
        topBar = topBar ?: {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedDetail != null) {
                // Show the favorite recipe details pane
                FavoriteRecipeDetailsPane(
                    recipeDetails = selectedDetail,
                    profileViewModel = profileViewModel,
                    onBackClick = {
                        // Return to the list of favorites
                        selectedDetail = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Otherwise, show either the empty UI or the RecipeList
                if (favorites.isEmpty()) {
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
                } else {
                    // Display the RecipeList if we have favorites
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
