package com.example.smartreciperecommenderapp.ui.ProfileScreen.settingsScreen

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
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onEditAccount: () -> Unit,
    onLogout: () -> Unit,
    onResetNavigatedToLoggedIn: () -> Unit
) {
    val userName = profileViewModel.userName.observeAsState("Guest").value
    val userAvatarUrl = profileViewModel.userAvatarUrl.observeAsState(null).value

    var isEditing by remember { mutableStateOf(false) }
    // 新的显示名输入状态
    var newDisplayName by remember { mutableStateOf(userName) }
    // 错误信息提示
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage ?: "")
            errorMessage = null
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Inform Page") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (userAvatarUrl != null) {
                AsyncImage(
                    model = userAvatarUrl,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 如果在编辑模式，则显示TextField，否则显示当前用户名
            if (isEditing) {
                OutlinedTextField(
                    value = newDisplayName,
                    onValueChange = { newDisplayName = it },
                    label = { Text("New Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = {
                        // Cancel editing
                        isEditing = false
                        newDisplayName = userName // 恢复原来的用户名称
                    }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        // Save new display name
                        profileViewModel.updateDisplayName(
                            newDisplayName,
                            onSuccess = {
                                isEditing = false
                            },
                            onFailure = {
                                errorMessage = it
                            }
                        )
                    }) {
                        Text("Save")
                    }
                }
            } else {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 编辑账号信息按钮
                Text(
                    text = "Edit Account Inform",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        isEditing = true
                        // 可以在这里调用 onEditAccount() 如果需要在外部监听
                        onEditAccount()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 分隔线
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(16.dp))

            // 登出按钮
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.clickable {
                    onResetNavigatedToLoggedIn()
                    onLogout()
                }
            )
        }
    }
}