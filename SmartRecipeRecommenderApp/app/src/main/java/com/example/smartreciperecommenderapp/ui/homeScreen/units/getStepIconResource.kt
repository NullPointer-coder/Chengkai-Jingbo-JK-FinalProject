package com.example.smartreciperecommenderapp.ui.homeScreen.units

import androidx.compose.runtime.Composable
import com.example.smartreciperecommenderapp.R

@Composable
fun getStepIconResource(directionDescription: String): Int {
    val descLower = directionDescription.lowercase()

    return when {
        // Wash, rinse, clean
        descLower.contains("wash") || descLower.contains("rinse") || descLower.contains("clean") -> R.drawable.ic_wash

        // Cutting, chopping, slicing
        descLower.contains("chop") || descLower.contains("cut") || descLower.contains("slice") -> R.drawable.ic_knife

        // Stirring, mixing, whisking
        descLower.contains("mix") || descLower.contains("stir") || descLower.contains("whisk") -> R.drawable.ic_mix_bowl

        // Boiling or cooking on the stove
        descLower.contains("boil") || descLower.contains("cook") -> R.drawable.ic_pot

        // Baking, roasting, oven usage
        descLower.contains("bake") || descLower.contains("roast") || descLower.contains("oven") -> R.drawable.ic_oven

        // Microwave heating or reheating
        descLower.contains("microwave") || descLower.contains("reheat") -> R.drawable.ic_microwave

        // Cooling, chilling, refrigerating, freezing
        descLower.contains("cool") || descLower.contains("chill") ||
                descLower.contains("refrigerate") || descLower.contains("freeze") -> R.drawable.ic_fridge

        // Pouring, pouring, adding
        descLower.contains("add") || descLower.contains("top") || descLower.contains("sprinkle")
                || descLower.contains("season") || descLower.contains("dollop") || descLower.contains("powder") -> R.drawable.ic_seasoning
        // Grilling
        descLower.contains("grill") -> R.drawable.ic_grill

        // Frying, sautéing
        descLower.contains("fry") || descLower.contains("saute") || descLower.contains("sauté") -> R.drawable.ic_pan

        // No specific keywords matched, use a generic cooking icon
        else -> R.drawable.ic_cooking_generic
    }

}