package com.example.smartreciperecommenderapp.ui.IngredientScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientScreen(navController: NavController) {
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
                        navController.navigate("barcodeScannerScreen")
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Scan Barcode")
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
        }
    }
}
