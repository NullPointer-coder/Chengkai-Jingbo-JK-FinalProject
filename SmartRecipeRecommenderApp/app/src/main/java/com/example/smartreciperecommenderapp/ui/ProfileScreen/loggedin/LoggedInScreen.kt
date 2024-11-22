package com.example.smartreciperecommenderapp.ui.ProfileScreen.loggedin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedInScreen(
    profileViewModel: ProfileViewModel,
    onMyFavoriteClick: () -> Unit,
    onFavoriteCuisinesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val userName = profileViewModel.userName.observeAsState("Guest").value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = userName, style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = {profileViewModel.logout()}) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                ActionRow(
                    icon = Icons.Default.Favorite,
                    text = "My Favorite",
                    onClick = onMyFavoriteClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                ActionRow(
                    icon = Icons.Default.Folder,
                    text = "Favorite Cuisines",
                    onClick = onFavoriteCuisinesClick
                )
            }
        }
    )
}

@Composable
fun ActionRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        ClickableText(
            text = AnnotatedString(text),
            onClick = { onClick() },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
