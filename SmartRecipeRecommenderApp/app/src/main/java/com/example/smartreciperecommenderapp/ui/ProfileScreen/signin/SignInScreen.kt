package com.example.smartreciperecommenderapp.ui.ProfileScreen.signin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.example.smartreciperecommenderapp.data.repository.LoginResult

import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@Composable
fun SignInScreen(
    profileViewModel: ProfileViewModel,
    onSignInSuccess: () -> Unit, // Callback for successful sign-in
    onSignInFailed: (String) -> Unit // Callback for failed sign-in
) {
    // 绑定 ViewModel 的状态
    val loginResult by profileViewModel.loginResult.observeAsState()
    var email by remember { mutableStateOf(profileViewModel.temporaryEmail.value ?: "") }
    var password by remember { mutableStateOf(profileViewModel.temporaryPassword.value ?: "") }
    var staySignedIn by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) } // 控制按钮加载状态

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome message
        Text(
            text = "Welcome, Sign in to get \nmore Fridgify functions",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 18.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email input field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("e.g., simple@example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        var showResetDialog by remember { mutableStateOf(false) }
        var resetEmail by remember { mutableStateOf("") }
        val context = LocalContext.current

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Password") },
                text = {
                    Column {
                        Text("Enter your email to receive a password reset link:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            profileViewModel.resetPassword(resetEmail,
                                onSuccess = {
                                    showResetDialog = false
                                    Toast.makeText(context, "Password reset email sent successfully.", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { error ->
                                    showResetDialog = false
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Forgot password 按钮
        TextButton(onClick = { showResetDialog = true }) {
            Text(text = "Forgot your password?", style = MaterialTheme.typography.bodySmall)
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Sign in button
        Button(
            onClick = {
                isLoading = true // 开始加载
                profileViewModel.updateTemporaryCredentials(email, password) // 更新临时凭证
                profileViewModel.login(email, password) { error ->
                    isLoading = false // 停止加载
                    onSignInFailed(error) // 登录失败处理
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading // 禁用按钮防止重复点击
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(text = "Sign In", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

}
