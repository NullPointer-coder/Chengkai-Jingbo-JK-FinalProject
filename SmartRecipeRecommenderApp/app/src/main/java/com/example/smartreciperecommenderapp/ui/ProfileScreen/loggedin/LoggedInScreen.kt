package com.example.smartreciperecommenderapp.ui.ProfileScreen.loggedin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

/**
 * Screen displayed when the user is logged in.
 * Shows the user's display name and provides quick navigation to favorite items and settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedInScreen(
    profileViewModel: ProfileViewModel,
    onMyFavoriteClick: () -> Unit,
    onFavoriteCuisinesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Observe the user's display name from the ViewModel
    val displayName = profileViewModel.userName.observeAsState("Guest").value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display the user's name in bold
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        // Settings button icon on the top right
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Settings"
                            )
                        }
                    }
                },
            )
        },
        content = { innerPadding ->
            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // A row that navigates to "My Favorite" items when clicked
                ActionRow(
                    icon = Icons.Default.Favorite,
                    text = "My Favorite",
                    onClick = onMyFavoriteClick
                )

                // Add more rows for other actions as needed
                // For example:
                // ActionRow(
                //     icon = Icons.Default.FavoriteBorder,
                //     text = "Favorite Cuisines",
                //     onClick = onFavoriteCuisinesClick
                // )
            }
        }
    )
}

/**
 * A reusable row component that displays an icon and text.
 * Clicking on this row triggers the provided onClick action.
 */
@Composable
fun ActionRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display the provided icon
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Display the provided text and make the entire row clickable
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.clickable { onClick() }
        )
    }
}
