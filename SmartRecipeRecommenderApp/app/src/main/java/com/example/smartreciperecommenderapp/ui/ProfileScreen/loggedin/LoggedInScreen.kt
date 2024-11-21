package com.example.smartreciperecommenderapp.ui.ProfileScreen.loggedin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.smartreciperecommenderapp.ui.ProfileScreen.ProfileViewModel

@Composable
fun LoggedInScreen(profileViewModel: ProfileViewModel) {
    val userName by profileViewModel.userName.observeAsState("Guest")
    val avatarUrl by profileViewModel.userAvatarUrl.observeAsState(null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        avatarUrl?.let {
            Image(painter = rememberAsyncImagePainter(it),
                contentDescription = "User Avatar"
            )
        }

        Text(text = "Welcome, $userName", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { profileViewModel.logout() }) {
            Text("Logout")
        }
    }
}
