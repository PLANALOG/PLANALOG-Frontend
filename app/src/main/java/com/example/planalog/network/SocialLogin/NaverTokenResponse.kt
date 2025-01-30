package com.example.planalog.network.SocialLogin

data class NaverTokenResponse(
    val resultType: String,
    val error: String?,
    val success: accessTokenSuccessResponse?
)

data class accessTokenSuccessResponse(
    val accessToken: String,
    val refreshToken: String,
)
