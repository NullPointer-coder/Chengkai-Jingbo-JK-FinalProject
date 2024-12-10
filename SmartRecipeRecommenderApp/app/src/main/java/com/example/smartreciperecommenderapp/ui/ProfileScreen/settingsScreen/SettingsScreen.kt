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

/**
 * A screen to display and update the user's personal information.
 * Allows the user to edit their display name, view their avatar,
 * and provides a logout option.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onEditAccount: () -> Unit,
    onLogout: () -> Unit,
    onResetNavigatedToLoggedIn: () -> Unit
) {
    // Observe the user's display name and avatar URL
    val userName = profileViewModel.userName.observeAsState("Guest").value
    val userAvatarUrl = profileViewModel.userAvatarUrl.observeAsState(null).value

    var isEditing by remember { mutableStateOf(false) }
    // Local state for the new display name input field
    var newDisplayName by remember { mutableStateOf(userName) }
    // State for handling and displaying error messages
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Display error messages as a snackbar
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage ?: "")
            errorMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Information") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // Main content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // If user has an avatar URL, display it, otherwise show a default icon
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

            // If the user is in editing mode, show an input field for the new display name
            // Otherwise, show the current display name with an option to edit
            if (isEditing) {
                OutlinedTextField(
                    value = newDisplayName,
                    onValueChange = { newDisplayName = it },
                    label = { Text("New Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        // Cancel editing and revert to the original username
                        isEditing = false
                        newDisplayName = userName
                    }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        // Save the updated display name
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

                // A clickable text to switch to editing mode
                Text(
                    text = "Edit Account Info",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        isEditing = true
                        // Call onEditAccount if needed to handle external logic
                        onEditAccount()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // A divider line
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(16.dp))

            // Logout text: clicking it logs the user out and resets navigation states
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
