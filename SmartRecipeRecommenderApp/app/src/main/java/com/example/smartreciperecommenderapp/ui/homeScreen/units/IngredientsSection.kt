package com.example.smartreciperecommenderapp.ui.homeScreen.units

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreciperecommenderapp.ui.api.IngredientsWrapper

// Attempt to parse the ingredient description to separate quantity from name.
// For example, if "1/2 cup chopped onion", "1/2 cup" is considered quantity and "chopped onion" is the name part.
// If parsing fails, treat the entire line as the ingredient name.
@Composable
fun formatIngredient(ingredientDescription: String): Pair<String, String> {
    val parts = ingredientDescription.split(" ", limit = 2)
    return if (parts.size >= 2 && parts[0].matches(Regex(".*\\d.*"))) {
        // If the first part contains digits, assume it's a quantity (e.g. "1/2", "2", "4 cups").
        Pair(parts[0], parts[1])
    } else {
        // No numeric quantity detected, return an empty quantity and full description as name.
        Pair("", ingredientDescription)
    }
}

@Composable
fun IngredientsSection(ingredients: IngredientsWrapper) {
    Text(
        text = "Ingredients",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 12.dp)
    )

    ingredients.ingredient.forEachIndexed { index, ingredient ->
        // Extract quantity and name from the ingredient description
        val (quantity, name) = formatIngredient(ingredient.ingredient_description)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .fillMaxWidth()
        ) {
            // Use Canvas to draw a small circle instead of a large icon, for a cleaner look
            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.size(10.dp)) {
                drawCircle(color = primaryColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // If we have a quantity, emphasize it by making it bold.
            if (quantity.isNotEmpty()) {
                Text(
                    text = "$quantity ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // If no quantity, just display the whole description.
                Text(
                    text = ingredient.ingredient_description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Add a divider between ingredients (except after the last one), for clearer separation
        if (index < ingredients.ingredient.size - 1) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}
