package com.example.smartreciperecommenderapp.ui.IngredientScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import java.util.*
import kotlin.math.roundToInt

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


        // 当组件首次进入时或重新进入时，加载本地数据（并进行同步）
        LaunchedEffect(key1 = true) {
            ingredientViewModel.loadIngredients()
        }

        // 当 ingredientName 改变时，防抖搜索
        LaunchedEffect(ingredientName) {
            searchJob?.cancel()
            if (ingredientName.isNotBlank()) {
                searchJob = coroutineScope.launch {
                    delay(1000) // 等待用户停止输入1秒后再搜索
                    qrScannerViewModel.fetchNutrientsByName(ingredientName)
                }
            } else {
                // 输入为空则清空搜索结果
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

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("My Ingredients") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // 搜索和扫描栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = ingredientName,
                        onValueChange = {
                            ingredientName = it
                            // 当用户改变输入时清空之前的选择
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

                // 显示搜索结果列表
                if (searchedFoods.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) // 给搜索结果一个固定高度上限
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
                                        // 当用户选择了某个食材后直接跳转详情页
                                        navController.navigate(Screen.ProductDetail.route)
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(text = food.food_name)
                            }
                        }
                    }
                }

                // 展示本地数据库中的ingredients列表
                // 当搜索栏下方无需滚动时，在下方展示用户已有的ingredients
                Spacer(modifier = Modifier.height(16.dp))

                if (localIngredients.isNotEmpty()) {
                    Text(
                        text = "Your Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(bottom = 65.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(localIngredients) { ingredient ->
                            IngredientItemCard(
                                ingredient = ingredient,
                                onDeleteClick = { ingredientViewModel.deleteIngredient(ingredient) },
                                onEditClick = {
                                    editingIngredient = ingredient
                                    showEditDialog = true
                                }
                            )
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
                            color = Color.Gray, // 字体颜色改为灰色
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
            // 图片部分
            Image(
                painter = rememberAsyncImagePainter(model = ingredient.imageUrl ?: R.drawable.placeholder),
                contentDescription = ingredient.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 文字部分
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

            // 操作按钮
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(80.dp)
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = { onEditClick(ingredient) }, // 传递当前 ingredient
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
        daysDiff < 0 -> Color(0xFFFFCDD2) // 已过期，浅红色
        daysDiff < 3 -> Color(0xFFFFFFE0) // 即将过期，浅黄色
        else -> Color(0xFFC8E6C9)        // 保质期内，浅绿色
    }
}

fun calculateRemainingDays(expiryDate: Date): Int {
    val now = System.currentTimeMillis()
    val diff = expiryDate.time - now
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}
