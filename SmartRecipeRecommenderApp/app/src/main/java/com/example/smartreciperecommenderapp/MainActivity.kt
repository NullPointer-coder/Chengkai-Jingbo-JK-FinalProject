package com.example.smartreciperecommenderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.tasks.await

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import com.example.smartreciperecommenderapp.data.model.User
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel
import com.example.smartreciperecommenderapp.ui.ProfileScreen.loggedout.LoggedOutScreen
import com.example.smartreciperecommenderapp.ui.theme.SmartRecipeRecommenderAppTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartRecipeRecommenderAppTheme {

                // 直接测试 LoggedOutScreen 或 LoggedInScreen
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                }
            }
        }
    }

}
