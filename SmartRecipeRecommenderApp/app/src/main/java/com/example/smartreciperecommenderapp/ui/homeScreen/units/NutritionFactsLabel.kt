package com.example.smartreciperecommenderapp.ui.homeScreen.units

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartreciperecommenderapp.ui.api.Serving

@Composable
fun NutritionFactsLabel(serving: Serving) {
    // This composable displays a nutrition facts style label
    val borderColor = Color.Black
    val textColor = Color.Black
    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        color = textColor
    )
    val subtitleStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        color = textColor
    )
    val normalTextStyle = MaterialTheme.typography.bodySmall.copy(color = textColor)
    val boldTextStyle = MaterialTheme.typography.bodySmall.copy(color = textColor, fontWeight = FontWeight.Bold)

    // Helper function to display a nutrient row with optional bold text
    @Composable
    fun NutrientRow(name: String, value: String, boldName:Boolean = false, boldValue: Boolean = false) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                name,
                style = if (boldName) boldTextStyle else normalTextStyle,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                value,
                style = if (boldValue) boldTextStyle else normalTextStyle,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Box(
        modifier = Modifier
            .widthIn(min = 200.dp)
            .border(BorderStroke(2.dp, borderColor))
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Nutrition Facts", style = titleStyle)

            // Divider for styling
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 2.dp,
                color = borderColor
            )

            // Serving Size row
            NutrientRow("Serving Size", serving.serving_size, boldName = true, boldValue = true)
            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 2.dp,
                color = borderColor
            )

            // Calories
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Amount Per Serving", style = normalTextStyle, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Calories", style = subtitleStyle)
                Text("${serving.calories.toInt()}", style = subtitleStyle, fontSize = 24.sp)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 4.dp,
                color = borderColor
            )

            // Daily Values label
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("% Daily Values*", style = normalTextStyle, fontStyle = FontStyle.Italic)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Macro and nutrients rows
            serving.fat?.let {
                NutrientRow("Total Fat", "${it}g", boldName = true, boldValue = true)
            }
            serving.saturated_fat?.let {
                NutrientRow("  Saturated Fat", "${it}g")
            }
            serving.trans_fat?.let {
                NutrientRow("  Trans Fat", "${it}g")
            }
            serving.polyunsaturated_fat?.let {
                NutrientRow("  Polyunsaturated Fat", "${it}g")
            }
            serving.monounsaturated_fat?.let {
                NutrientRow("  Monounsaturated Fat", "${it}g")
            }

            Spacer(modifier = Modifier.height(8.dp))

            serving.cholesterol?.let {
                NutrientRow("Cholesterol", "${it}mg", boldName = true, boldValue = true)
            }
            serving.sodium?.let {
                NutrientRow("Sodium", "${it}mg", boldName = true, boldValue = true)
            }
            serving.carbohydrate?.let {
                NutrientRow("Total Carbohydrate", "${it}g", boldName = true, boldValue = true)
            }
            serving.fiber?.let {
                NutrientRow("  Dietary Fiber", "${it}g")
            }
            serving.sugar?.let {
                NutrientRow("  Sugars", "${it}g")
            }

            Spacer(modifier = Modifier.height(8.dp))

            serving.protein?.let {
                NutrientRow("Protein", "${it}g", boldName = true, boldValue = true)
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(thickness = 2.dp, color = borderColor)
            Spacer(modifier = Modifier.height(4.dp))

            // Micronutrients
            serving.calcium?.let {
                NutrientRow("Calcium", "${it}mg")
            }
            serving.iron?.let {
                NutrientRow("Iron", "${it}mg")
            }
            serving.potassium?.let {
                NutrientRow("Potassium", "${it}mg")
            }
            serving.vitamin_a?.let {
                NutrientRow("Vitamin A", "${it}mg")
            }
            serving.vitamin_c?.let {
                NutrientRow("Vitamin C", "${it}mg")
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 2.dp, color = borderColor)

            Spacer(modifier = Modifier.height(4.dp))
            // Footer note
            Text(
                "* The % Daily Value (DV) tells you how much a nutrient in a serving of food contributes to a daily diet. 2,000 calories a day is used for general nutrition advice.",
                style = normalTextStyle,
                fontSize = 10.sp
            )
        }
    }
}