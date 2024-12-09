package com.example.smartreciperecommenderapp.ui.IngredientScreen.barcodeResult

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
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

    var nameText by remember { mutableStateOf(currentIngredient.name) }
    val units = listOf("g", "kg", "ml", "L", "pieces")
    var quantityText by remember { mutableStateOf(currentIngredient.quantity.toString()) }
    var selectedUnit by remember {
        mutableStateOf(
            if (units.contains(currentIngredient.unit)) currentIngredient.unit else units.first()
        )
    }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    var expiryDateText by remember { mutableStateOf(currentIngredient.expiryDate?.let {
        SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(it)
    } ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    // 新增：数量输入错误状态
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

    // 使用 Box 包裹 Card 和 FAB
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        // 卡片中包含可滚动内容
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            // 使用LazyColumn让内容可以滚动
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 顶部导航行
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

                // 图片展示
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

                // Name & Search行
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

                // 搜索结果列表
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

                // Quantity字段 - 修改为数字输入
                item {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { input ->
                            // 使用正则表达式允许数字和一个可选的小数点
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
                                    text = "请输入有效的数字",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )
                }

                // Unit字段
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

                // Expiry Date字段
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker = true
                            }
                    ) {
                        Log.d("ProductDetailScreen", "id: ${ingredient?.id  }")
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

                // Calories & Fat 卡片
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
                            // Calories
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
                                    text = (currentIngredient.calories?.toString() ?: "None") + " kcal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Fat
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
                                    text = (currentIngredient.fat?.toString() ?: "None") + " g",
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

        val canSave = !quantityError &&
                quantityText.isNotBlank() &&
                ingredient!!.calories != null && !ingredient!!.calories?.isNaN()!! &&
                ingredient!!.fat != null && !ingredient!!.fat?.isNaN()!!

        FloatingActionButton(
            onClick = {
                if (canSave) {
                    // 解析数量输入
                    val quantity = quantityText.toDoubleOrNull() ?: 0.0

                    // 解析到期日期
                    val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                    val expiryDate = try {
                        sdf.parse(expiryDateText)
                    } catch (e: Exception) {
                        null
                    }

                    // 创建更新后的 Ingredient 实例
                    val updatedIngredient = currentIngredient.copy(
                        name = nameText,
                        quantity = quantity,
                        unit = selectedUnit,
                        expiryDate = expiryDate
                    )

                    // 保存更新后的 ingredient
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
                .alpha(if (canSave) 1f else 0.3f), // 不可用时降低透明度
            containerColor = if (canSave) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
            contentColor = if (canSave) MaterialTheme.colorScheme.onPrimaryContainer else Color.LightGray
        ) {
            Text("Save")
        }

    }
}

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
                        init(selectedYear, selectedMonth, selectedDay) { _, year, month, dayOfMonth ->
                            selectedYear = year
                            selectedMonth = month
                            selectedDay = dayOfMonth
                        }
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
                onDateSelected(parsedDate)
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
