package com.example.planalog.network.friend.response

data class FriendResponse(
    val resultType: String,
    val success: Any?,
    val error: ErrorResponse?
)

data class Friend(
    val friendId: Int,
    val id: Int,
    val name: String,
    val email: String,
    val nickname: String,
    val introduction: String,
    val link: String
)

data class ErrorResponse(
    val errorCode: String,
    val reason: String
)