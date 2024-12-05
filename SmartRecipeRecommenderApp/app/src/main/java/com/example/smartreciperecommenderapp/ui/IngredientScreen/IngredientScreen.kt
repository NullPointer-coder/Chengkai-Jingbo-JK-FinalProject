package com.example.smartreciperecommenderapp.ui.IngredientScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.R
import com.example.smartreciperecommenderapp.data.model.CategoryEntity
import com.example.smartreciperecommenderapp.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientScreen(navController: NavController, categories: List<CategoryEntity>) {
    var ingredientName by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf("Milk", "Eggs", "Cheese") }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text("Add Ingredient") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        // 这里可以加入导航跳转逻辑，例如跳转到条形码扫描页面
                        navController.navigate(Screen.BarcodeScanner.route)
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_barcode_scanner), contentDescription = "Scan Barcode")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (ingredientName.isNotBlank()) {
                        ingredients.add(ingredientName)
                        ingredientName = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(ingredients.size) { index ->
                    Text(
                        text = ingredients[index],
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(categories.size) { index ->
                    val category = categories[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Count: ${category.productCount}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
