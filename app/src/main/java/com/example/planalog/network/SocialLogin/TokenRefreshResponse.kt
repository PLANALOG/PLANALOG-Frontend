package com.example.planalog.network.SocialLogin

data class TokenRefreshResponse(
    val resultType: String,
    val error: String?,
    val success: refreshTokenSuccessResponse?
)

data class refreshTokenSuccessResponse(
    val accessToken: String,
    val refreshToken: String?
)
