package com.example.smartreciperecommenderapp.ui.ProfileScreen.signin

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

import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@Composable
fun SignInScreen(profileViewModel: ProfileViewModel,
                 onSignInSuccess: () -> Unit, // Callback for successful sign-in
                 onSignInFailed: (String) -> Unit) {
    var email by remember { mutableStateOf(profileViewModel.getTemporaryEmail()) }
    var password by remember { mutableStateOf(profileViewModel.getTemporaryPassword()) }
    var staySignedIn by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome message
        Text(
            text = "Welcome, Sign in to get \nrecipe recommendations",
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

        // Forgot password
        TextButton(onClick = { }) {
            Text(text = "Forgot your password?", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stay signed in toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Stay Signed In", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = staySignedIn, onCheckedChange = { staySignedIn = it })
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign in button
        Button(

            onClick = {

                profileViewModel.performSensitiveAction(
                    email = email,
                    password = password,
                    onSuccess = { profileViewModel.login(email, password) }, // Navigate or show success message
                    onFailure = { errorMessage ->
                        profileViewModel.updateTemporaryCredentials(email, password)
                        onSignInFailed(errorMessage)
                    } // Show error feedback
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Sign In", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
