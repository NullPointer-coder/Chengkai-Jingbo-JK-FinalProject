package com.example.smartreciperecommenderapp.ui.IngredientScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.R
import com.example.smartreciperecommenderapp.data.model.Ingredient
import com.example.smartreciperecommenderapp.ui.IngredientScreen.camera.QRScannerViewModel
import com.example.smartreciperecommenderapp.ui.api.FatSecretFood
import com.example.smartreciperecommenderapp.ui.navigation.LoginPrompt
import com.example.smartreciperecommenderapp.ui.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientScreen(
    navController: NavController,
    isLoggedIn: Boolean,
    qrScannerViewModel: QRScannerViewModel,
    ingredientViewModel: IngredientViewModel
) {
    if (!isLoggedIn) {
        LoginPrompt(
            message = "You are not logged in yet, please log in first to view and manage your ingredients.",
            onLoginClick = { navController.navigate(Screen.Account.route) }
        )
    } else {
        var ingredientName by remember { mutableStateOf("") }

        val searchedFoods by qrScannerViewModel.searchedFoods.collectAsState()
        var selectedFood by remember { mutableStateOf<FatSecretFood?>(null) }

        val coroutineScope = rememberCoroutineScope()
        var searchJob by remember { mutableStateOf<Job?>(null) }

        LaunchedEffect(key1 = true) {
            ingredientViewModel.loadIngredients()
        }

        LaunchedEffect(ingredientName) {
            searchJob?.cancel()
            if (ingredientName.isNotBlank()) {
                searchJob = coroutineScope.launch {
                    delay(1000)
                    qrScannerViewModel.fetchNutrientsByName(ingredientName)
                }
            } else {
                qrScannerViewModel.clearSearchedFoods()
                selectedFood = null
            }
        }

        var showEditDialog by remember { mutableStateOf(false) }
        var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }

        val localIngredients by ingredientViewModel.ingredients.collectAsState()

        if (showEditDialog && editingIngredient != null) {
            EditIngredientDialog(
                ingredient = editingIngredient!!,
                onDismiss = { showEditDialog = false },
                onSave = { updatedIngredient ->
                    ingredientViewModel.updateIngredientQuantity(updatedIngredient.instanceId, updatedIngredient.quantity)
                    showEditDialog = false
                }
            )
        }

        val expiredIngredients = localIngredients.filter { it.expiryDate?.let { date -> calculateRemainingDays(date) < 0 } == true }
        val nonExpiredIngredients = localIngredients.filter { it.expiryDate?.let { date -> calculateRemainingDays(date) >= 0 } != false }

        var expiredExpanded by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("My Ingredients") }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = ingredientName,
                        onValueChange = {
                            ingredientName = it
                            selectedFood = null
                        },
                        label = { Text("Search Ingredient") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.BarcodeScanner.route)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_barcode_scanner),
                            contentDescription = "Scan Barcode"
                        )
                    }
                }

                if (searchedFoods.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(top = 16.dp)
                    ) {
                        items(searchedFoods.size) { index ->
                            val food = searchedFoods[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFood = food
                                        qrScannerViewModel.updateSelectedFood(food)
                                        qrScannerViewModel.clearSearchedFoods()
                                        navController.navigate(Screen.ProductDetail.route)
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(text = food.food_name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (localIngredients.isNotEmpty()) {
                    Text(
                        text = "Your Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val expiredCount = expiredIngredients.size
                    if (expiredCount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expiredExpanded = !expiredExpanded }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expired ($expiredCount)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                imageVector = if (expiredExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = if (expiredExpanded) "Collapse" else "Expand"
                            )
                        }

                        AnimatedVisibility(visible = expiredExpanded) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = expiredIngredients,
                                    key = { ingredient -> ingredient.instanceId }
                                ) { ingredient ->
                                    IngredientItemCard(
                                        ingredient = ingredient,
                                        onDeleteClick = {
                                            ingredientViewModel.deleteIngredient(
                                                ingredient
                                            )
                                        },
                                        onEditClick = { ing ->
                                            editingIngredient = ing
                                            showEditDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    val nonExpiredCount = nonExpiredIngredients.size
                    if (nonExpiredCount > 0) {
                        Text(
                            text = "Non-Expired ($nonExpiredCount)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .padding(bottom = 65.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = nonExpiredIngredients,
                                key = { ingredient -> ingredient.instanceId }
                            ) { ingredient ->
                                IngredientItemCard(
                                    ingredient = ingredient,
                                    onDeleteClick = { ingredientViewModel.deleteIngredient(ingredient) },
                                    onEditClick = { ing ->
                                        editingIngredient = ing
                                        showEditDialog = true
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,

                    ) {
                        Text(
                            text = "You have no saved ingredients.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientItemCard(
    ingredient: Ingredient,
    onDeleteClick: () -> Unit,
    onEditClick: (Ingredient) -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LaunchedEffect(visible) {
            if (!visible) {
                delay(300)
                onDeleteClick()
            }
        }

        val cardColor = determineCardColor(ingredient.expiryDate)

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = cardColor)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = ingredient.imageUrl ?: R.drawable.placeholder),
                    contentDescription = ingredient.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${ingredient.quantity} ${ingredient.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    ingredient.calories?.let {
                        Text(
                            text = "Calories: $it kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    ingredient.fat?.let {
                        Text(
                            text = "Fat: $it g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    ingredient.expiryDate?.let {
                        val remainingDays = calculateRemainingDays(it)
                        Text(
                            text = if (remainingDays < 0) "Expired ${0 - remainingDays} day(s)"
                            else if (remainingDays == 0) "Expires today"
                            else "Expires in: $remainingDays day(s)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (remainingDays <= 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.height(80.dp)
                ) {
                    IconButton(
                        onClick = {
                            visible = false
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(
                        onClick = { onEditClick(ingredient) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun EditIngredientDialog(
    ingredient: Ingredient,
    onDismiss: () -> Unit,
    onSave: (Ingredient) -> Unit
) {
    var quantity by remember { mutableStateOf(ingredient.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${ingredient.name}", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                Text("Update Quantity", style = MaterialTheme.typography.bodyMedium)
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter new quantity") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedQuantity = quantity.toDoubleOrNull()
                    if (updatedQuantity != null) {
                        val updatedIngredient = ingredient.copy(quantity = updatedQuantity)
                        onSave(updatedIngredient)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun determineCardColor(expiryDate: Date?): Color {
    if (expiryDate == null) return MaterialTheme.colorScheme.surface

    val daysDiff = calculateRemainingDays(expiryDate)
    return when {
        daysDiff < 0 -> Color(0xFFFFCDD2)
        daysDiff < 3 -> Color(0xFFFFFFE0)
        else -> Color(0xFFC8E6C9)
    }
}

fun calculateRemainingDays(expiryDate: Date): Int {
    val now = System.currentTimeMillis()
    val diff = expiryDate.time - now
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}
