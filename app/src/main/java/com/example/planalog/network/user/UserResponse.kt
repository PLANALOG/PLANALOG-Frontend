package com.example.planalog.network.user

data class UserResponse(
    val resultType: String,
    val error: String?,
    val success: UserInfo?
)

data class UserInfo(
    val userId: Int,
    val email: String,
    val platform: String,
    val name: String,
    val nickname: String,
    val type: String? = "",
    val introduction: String,
    val link: String,
    val profileImage: String?
)
