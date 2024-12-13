package com.example.smartreciperecommenderapp.ui.ProfileScreen.favoritecuisines

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteCuisinesScreen (
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
){
    Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("Favorite Cuisines") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // 使用 AutoMirrored 版本
                        contentDescription = "Back"
                    )
                }
            }
        )
    }
    ) { innerPadding ->

    }
}