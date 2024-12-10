package com.example.smartreciperecommenderapp.ui.ProfileScreen.signin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.filled.*
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

/**
 * The SignInScreen allows the user to sign in using their email and password.
 * It includes options for:
 * - Viewing/hiding the password
 * - Resetting the password if forgotten
 * - Displaying loading indicators while processing the sign-in request
 *
 * @param profileViewModel The ViewModel handling user login logic
 * @param onSignInSuccess Callback triggered when the user signs in successfully
 * @param onSignInFailed Callback triggered when an error occurs during sign-in
 */
@Composable
fun SignInScreen(
    profileViewModel: ProfileViewModel,
    onSignInSuccess: () -> Unit,
    onSignInFailed: (String) -> Unit
) {
    // Observe the login result from the ViewModel
    val loginResult by profileViewModel.loginResult.observeAsState()
    var email by remember { mutableStateOf(profileViewModel.temporaryEmail.value ?: "") }
    var password by remember { mutableStateOf(profileViewModel.temporaryPassword.value ?: "") }
    var staySignedIn by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // State for password reset dialog
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Main layout
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

        // Password input field with toggle visibility
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

        // Display a dialog for password reset if needed
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
                            profileViewModel.resetPassword(
                                resetEmail,
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

        // Forgot password button
        TextButton(onClick = { showResetDialog = true }) {
            Text(text = "Forgot your password?", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign-in button with loading indicator
        Button(
            onClick = {
                isLoading = true
                profileViewModel.updateTemporaryCredentials(email, password)
                profileViewModel.login(email, password) { error ->
                    isLoading = false
                    onSignInFailed(error) // Handle sign-in failure
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoading
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
