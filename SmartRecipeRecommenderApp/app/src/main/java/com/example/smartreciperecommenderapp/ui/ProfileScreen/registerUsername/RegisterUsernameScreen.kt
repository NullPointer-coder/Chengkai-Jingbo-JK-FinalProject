package com.example.smartreciperecommenderapp.ui.ProfileScreen.registerUsername

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onError: (String) -> Unit,
    onResetNavigatedToSignIn: () -> Unit
) {
    // 使用 LiveData.observeAsState 获取用户名
    val username by profileViewModel.userName.observeAsState("")
    val context = LocalContext.current

    // 本地状态，用于临时输入管理
    var usernameInput by remember { mutableStateOf(username) }
    var isButtonEnabled by remember { mutableStateOf(false) }

    // 根据输入状态更新按钮状态
    LaunchedEffect(usernameInput) {
        isButtonEnabled = usernameInput.trim().isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Registration") },
                navigationIcon = {
                    IconButton(onClick = {
                        profileViewModel.resetLoginResult() // 重置登录结果
                        onResetNavigatedToSignIn()
                        val success = navController.popBackStack()
                        if (!success) {
                            navController.navigate("signin") {
                                popUpTo("signin") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                value = usernameInput,
                onValueChange = { usernameInput = it },
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
                    if (usernameInput.trim().isEmpty()) {
                        onError("Username cannot be empty!")
                    } else {
                        profileViewModel.updateUserName(usernameInput)
                        profileViewModel.resetLoginResult()
                        onUsernameEntered(usernameInput)
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