package com.example.smartreciperecommenderapp.ui.ProfileScreen

import androidx.compose.runtime.Composable
import com.example.smartreciperecommenderapp.data.model.User


@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    // 示例：加载用户数据
    profileViewModel.loadUser("mockUserId")

    // 示例：保存用户数据
    profileViewModel.saveUser("mockUserId", User(username = "NewUser", avatarUrl = null))
}

