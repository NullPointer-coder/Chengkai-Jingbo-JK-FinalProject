package com.example.smartreciperecommenderapp.ui.ProfileScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreciperecommenderapp.data.model.User
import com.example.smartreciperecommenderapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val user = userRepository.getUser(userId)
            println("Loaded user: $user")
        }
    }

    fun saveUser(userId: String, user: User) {
        viewModelScope.launch {
            userRepository.saveUser(userId, user)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userRepository.deleteUser(userId)
        }
    }
}
