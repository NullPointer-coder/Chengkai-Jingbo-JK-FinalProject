package com.example.smartreciperecommenderapp.ui.IngredientScreen.barcodeResult

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartreciperecommenderapp.ui.IngredientScreen.IngredientViewModel
import com.example.smartreciperecommenderapp.ui.IngredientScreen.camera.QRScannerViewModel
import com.example.smartreciperecommenderapp.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductDetailScreen(
    navController: NavController,
    qRScannerViewModel: QRScannerViewModel,
    ingredientViewModel: IngredientViewModel
) {
    val ingredient by qRScannerViewModel.ingredient.collectAsState()
    val searchedFoods by qRScannerViewModel.searchedFoods.collectAsState()

    // If no ingredient details are available, show a message.
    if (ingredient == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No product details available.")
        }
        return
    }

    val currentIngredient = ingredient!!

    // UI state variables for editable fields
    var nameText by remember { mutableStateOf(currentIngredient.name) }
    val units = listOf("g", "kg", "ml", "L", "pieces")
    var quantityText by remember { mutableStateOf(currentIngredient.quantity.toString()) }
    var selectedUnit by remember {
        mutableStateOf(
            if (units.contains(currentIngredient.unit)) currentIngredient.unit else units.first()
        )
    }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    var expiryDateText by remember {
        mutableStateOf(
            currentIngredient.expiryDate?.let {
                SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(it)
            } ?: ""
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    // Error state if the quantity is not a valid number
    var quantityError by remember { mutableStateOf(false) }

    // Show a date picker dialog if required
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                expiryDateText = sdf.format(selectedDate)
                showDatePicker = false
            },
            onDismissRequest = { showDatePicker = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            // Using a LazyColumn to allow scrolling for content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top row with a back button and title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            qRScannerViewModel.resetScan()
                            qRScannerViewModel.clearSearchedFoods()
                            nameText = ""
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ingredient Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Display the product image if available
                item {
                    val imageUrl = currentIngredient.imageUrl
                    if (imageUrl != null) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = "Product Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Name field and search button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Name") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                // Perform search only if the name is sufficiently long
                                if (nameText.isNotBlank() && nameText.length > 2) {
                                    qRScannerViewModel.fetchNutrientsByName(nameText)
                                } else {
                                    qRScannerViewModel.clearSearchedFoods()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text("Search")
                        }
                    }
                }

                // Display search results if any
                if (searchedFoods.isNotEmpty()) {
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            LazyColumn {
                                items(searchedFoods) { foodItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                qRScannerViewModel.updateSelectedFood(foodItem)
                                                nameText = foodItem.food_name
                                                qRScannerViewModel.clearSearchedFoods()
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Text(foodItem.food_name)
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }

                // Quantity field with validation for numeric input
                item {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { input ->
                            val regex = Regex("^\\d*\\.?\\d*\$")
                            if (regex.matches(input)) {
                                quantityText = input
                                quantityError = false
                            } else {
                                quantityError = true
                            }
                        },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = quantityError,
                        supportingText = {
                            if (quantityError) {
                                Text(
                                    text = "Please enter a valid number",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )
                }

                // Unit selection field with a dropdown menu
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                unitDropdownExpanded = true
                            }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = { },
                            label = { Text("Unit") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    unitDropdownExpanded = !unitDropdownExpanded
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Unit"
                                    )
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Expiry date field with a date picker
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker = true
                            }
                    ) {
                        Log.d("ProductDetailScreen", "id: ${ingredient?.id}")
                        OutlinedTextField(
                            value = expiryDateText,
                            onValueChange = { expiryDateText = it },
                            label = { Text("Expiry Date") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    showDatePicker = true
                                }) {
                                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            }
                        )
                    }
                }

                // Display calories & fat card only if both values are available
                if (currentIngredient.calories != null && currentIngredient.fat != null) {
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Calories info
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Calories",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Calories",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = (currentIngredient.calories.toString()) + " kcal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Fat info
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = "Fat",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Fat",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = (currentIngredient.fat.toString()) + " g",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Determine if the 'Save' button should be enabled
        val canSave = !quantityError &&
                quantityText.isNotBlank() &&
                ingredient!!.calories != null && !ingredient!!.calories!!.isNaN() &&
                ingredient!!.fat != null && !ingredient!!.fat!!.isNaN()

        // Save FloatingActionButton
        FloatingActionButton(
            onClick = {
                if (canSave) {
                    val quantity = quantityText.toDoubleOrNull() ?: 0.0
                    val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                    val expiryDate = try {
                        sdf.parse(expiryDateText)
                    } catch (e: Exception) {
                        null
                    }

                    // Create an updated ingredient object with the new values
                    val updatedIngredient = currentIngredient.copy(
                        name = nameText,
                        quantity = quantity,
                        unit = selectedUnit,
                        expiryDate = expiryDate
                    )

                    // Save the updated ingredient
                    ingredientViewModel.saveIngredient(
                        updatedIngredient,
                        onSuccess = {
                            navController.navigate(Screen.Ingredient.route)
                        },
                        onError = { errorMsg ->
                            Log.e("ProductDetailScreen", "Error saving ingredient: $errorMsg")
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = 20.dp)
                .alpha(if (canSave) 1f else 0.3f),
            containerColor = if (canSave) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
            contentColor = if (canSave) MaterialTheme.colorScheme.onPrimaryContainer else Color.DarkGray
        ) {
            Text("Save")
        }
    }
}

/**
 * A dialog for selecting a date using a DatePicker.
 * Automatically sets the minimum date to the current day so that past dates are disabled.
 */
@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismissRequest: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }

    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Date") },
        text = {
            AndroidView(
                factory = { ctx ->
                    android.widget.DatePicker(ctx).apply {
                        // Initialize the date picker with the currently selected date
                        init(selectedYear, selectedMonth, selectedDay) { _, year, month, dayOfMonth ->
                            selectedYear = year
                            selectedMonth = month
                            selectedDay = dayOfMonth
                        }

                        // Disable dates before today
                        val currentDate = Calendar.getInstance()
                        currentDate.set(Calendar.HOUR_OF_DAY, 0)
                        currentDate.set(Calendar.MINUTE, 0)
                        currentDate.set(Calendar.SECOND, 0)
                        currentDate.set(Calendar.MILLISECOND, 0)
                        minDate = currentDate.timeInMillis
                    }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val cal = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                }
                val selectedDate = cal.time

                val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedDate)
                val parsedDate = try {
                    sdf.parse(formattedDate)
                } catch (e: Exception) {
                    selectedDate
                }
                if (parsedDate != null) {
                    onDateSelected(parsedDate)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
