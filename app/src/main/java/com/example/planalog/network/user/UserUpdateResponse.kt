package com.example.planalog.network.user

data class UserUpdateResponse(
    val resultType: String,
    val error: String?,
    val success: UserSuccessResponse?
)

data class UserSuccessResponse(
    val userId: String,
    val email: String,
    val platform: String,
    val name: String,
    val nickname: String,
    val type: String,
    val introduction: String,
    val link: String,
    val createdAt: String,
    val updatedAt: String
)