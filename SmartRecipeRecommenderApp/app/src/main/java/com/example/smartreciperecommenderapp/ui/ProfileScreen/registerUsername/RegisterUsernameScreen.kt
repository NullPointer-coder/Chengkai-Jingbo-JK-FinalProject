package com.example.smartreciperecommenderapp.ui.ProfileScreen.registerUsername

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

/**
 * Screen displayed after a user account is created, prompting the user to provide a username.
 * Once a valid username is entered, the user is considered registered and can proceed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUsernameScreen(
    profileViewModel: ProfileViewModel,
    onUsernameEntered: (String) -> Unit,
    navController: NavController,
    onError: (String) -> Unit,
    onResetNavigatedToSignIn: () -> Unit
) {
    // Observe the current username from ViewModel (if any)
    val username by profileViewModel.userName.observeAsState("")
    val context = LocalContext.current

    // Local state for managing the username input field
    var usernameInput by remember { mutableStateOf(username) }
    var isButtonEnabled by remember { mutableStateOf(false) }

    // Update the "Register" button state based on the current input
    LaunchedEffect(usernameInput) {
        isButtonEnabled = usernameInput.trim().isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Registration") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Reset the login result and navigate back to sign-in if needed
                        profileViewModel.resetLoginResult()
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
        // Main content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Prompt the user to enter a username
            Text(
                text = "Enter a Username",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Username input field
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

            // Register button - only enabled if username is non-empty
            Button(
                onClick = {
                    if (usernameInput.trim().isEmpty()) {
                        onError("Username cannot be empty!")
                    } else {
                        // Update username in ViewModel and finalize registration
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
