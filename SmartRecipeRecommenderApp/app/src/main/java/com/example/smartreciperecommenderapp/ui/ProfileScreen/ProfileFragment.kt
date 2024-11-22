package com.example.smartreciperecommenderapp.ui.ProfileScreen

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.example.smartreciperecommenderapp.data.repository.UserRepository

class ProfileFragment : Fragment() {

    private val userRepository by lazy { UserRepository() } // 初始化 UserRepository
    private val profileViewModel: ProfileViewModel by lazy {
        ViewModelProvider(
            this,
            ProfileViewModelFactory(userRepository)
        )[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ProfileScreen(profileViewModel) // 将 ViewModel 传递给 ProfileScreen
            }
        }
    }
}