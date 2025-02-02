package com.example.planalog.network.SocialLogin

data class LogoutResponse(
    val resultType: String,
    val error: String,
    val success : Message?
)

data class Message(
    val message: String
)
