package com.example.smartreciperecommenderapp.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    @PropertyName("username") val username: String = "",
    @PropertyName("avatar_url") val avatarUrl: String? = null,
    @PropertyName("email") val email: String = "",
    @PropertyName("password") val password: String? = null
)