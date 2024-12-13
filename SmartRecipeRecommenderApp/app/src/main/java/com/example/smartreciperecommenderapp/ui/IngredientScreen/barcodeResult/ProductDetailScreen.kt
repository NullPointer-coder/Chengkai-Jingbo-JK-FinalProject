package com.example.smartreciperecommenderapp.ui.IngredientScreen.barcodeResult

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartreciperecommenderapp.R
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

    // 状态变量
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

    var quantityError by remember { mutableStateOf(false) }

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

    val imageUrl = currentIngredient.imageUrl
    val painter = rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(R.drawable.placeholder),
        error = painterResource(R.drawable.placeholder),
        onSuccess = {
            Log.d("ProductDetailScreen", "Image loaded successfully.")
        },
        onError = { state ->
            Log.e("ProductDetailScreen", "Error loading image: ${state.result.throwable}")
        }
    )

    val context = LocalContext.current

    // 可滚动布局
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 标题行（返回和标题）
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

            // 使用ElevatedCard包裹LazyColumn，并使用weight(1f)确保LazyColumn在剩余空间内滚动
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.elevatedCardElevation(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        if (imageUrl != null) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                elevation = CardDefaults.elevatedCardElevation(4.dp)
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = "Product Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f/9f),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

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

                    if (searchedFoods.isNotEmpty()) {
                        item {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                Column {
                                    searchedFoods.forEach { foodItem ->
                                        Column {
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
                    }

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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            Spacer(modifier = Modifier.height(120.dp))
        }

        val canSave = !quantityError &&
                quantityText.isNotBlank() &&
                ingredient!!.calories != null && !ingredient!!.calories!!.isNaN() &&
                ingredient!!.fat != null && !ingredient!!.fat!!.isNaN()

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

                    val updatedIngredient = currentIngredient.copy(
                        name = nameText,
                        quantity = quantity,
                        unit = selectedUnit,
                        expiryDate = expiryDate
                    )

                    ingredientViewModel.saveIngredient(
                        updatedIngredient,
                        onSuccess = {
                            navController.navigate(Screen.Ingredient.route)
                        },
                        onError = { errorMsg ->
                            Log.e("ProductDetailScreen", "Error saving ingredient: $errorMsg")
                            Toast.makeText(
                                context,
                                "No network connection available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else {
                    Toast.makeText(
                        context,
                        "Please ensure ingredient name is valid and nutrients are available.",
                        Toast.LENGTH_SHORT
                    ).show()
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
