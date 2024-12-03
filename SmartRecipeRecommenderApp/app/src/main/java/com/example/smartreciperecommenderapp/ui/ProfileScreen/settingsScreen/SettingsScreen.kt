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

            Spacer(modifier = Modifier.height(8.dp))

            // 用户名
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
                modifier = Modifier.clickable { onEditAccount() }
            )

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