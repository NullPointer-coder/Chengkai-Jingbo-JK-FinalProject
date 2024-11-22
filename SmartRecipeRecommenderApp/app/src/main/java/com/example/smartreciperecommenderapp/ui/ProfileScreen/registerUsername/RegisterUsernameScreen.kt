package com.example.smartreciperecommenderapp.ui.ProfileScreen.registerUsername

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.data.repository.LoginResult
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUsernameScreen(
    profileViewModel: ProfileViewModel,
    onUsernameEntered: (String) -> Unit,
    navController: NavController,
    onError: (String) -> Unit
) {
    var username by remember { mutableStateOf(profileViewModel.getUserName()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isButtonEnabled by remember { mutableStateOf(false) }

    // 检测输入变化
    LaunchedEffect(username) {
        isButtonEnabled = username.trim().isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Registration") },
                navigationIcon = {
                    IconButton(onClick = {
                        profileViewModel.resetLoginResult()
                        val success = navController.popBackStack()
                        if (!success) {
                            navController.navigate("signin") {
                                popUpTo("signin") { inclusive = true }
                            }
                        }
                    }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // 提示标题
            Text(
                text = "Enter a Username",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                placeholder = { Text("e.g., JohnDoe") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // 确认按钮
            Button(
                onClick = {
                    if (username.trim().isEmpty()) {
                        onError("Username cannot be empty!") // Call onError with error message
                    } else {
                        profileViewModel.updateUserName(username)
                        profileViewModel.resetLoginResult()
                        onUsernameEntered(username)
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
        }
    }
}
