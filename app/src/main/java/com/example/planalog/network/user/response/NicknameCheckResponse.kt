package com.example.planalog.network.user.response

data class NicknameCheckResponse(
    val resultType: String,
    val error: String?,
    val success: Success?
)

data class Success(
    val isDuplicated: Boolean
)
