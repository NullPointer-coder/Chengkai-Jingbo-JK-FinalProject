package com.example.smartreciperecommenderapp.ui.homeScreen.units

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartreciperecommenderapp.data.model.RecipeModel

@Composable
fun RecipeList(
    recipes: List<RecipeModel>,
    onRecipeClick: (RecipeModel) -> Unit,
    modifier: Modifier = Modifier
) {
    // Displays a list of recipes
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