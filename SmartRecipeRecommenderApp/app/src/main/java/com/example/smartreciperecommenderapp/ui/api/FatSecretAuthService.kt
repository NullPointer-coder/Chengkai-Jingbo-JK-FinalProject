package com.example.smartreciperecommenderapp.ui.api

import retrofit2.http.*

interface FatSecretAuthService {
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String = "c54d5f5ea7854c63aead29a4f99e37a9",
        @Field("client_secret") clientSecret: String = "1bdd74415ec44f8dbebf008d069857d3",
        @Field("scope") scope: String = "basic"
    ): AccessTokenResponse
}

data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val scope: String
)
