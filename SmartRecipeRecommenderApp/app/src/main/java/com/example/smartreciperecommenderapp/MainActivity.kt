package com.example.smartreciperecommenderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileScreen
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModelFactory

import com.example.smartreciperecommenderapp.ui.theme.SmartRecipeRecommenderAppTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userRepository = UserRepository()
        enableEdgeToEdge()
        setContent {
            SmartRecipeRecommenderAppTheme {


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProfileScreen(
                        profileViewModel = viewModel(factory = ProfileViewModelFactory(userRepository))
                    )
                }
            }
        }
    }

}
