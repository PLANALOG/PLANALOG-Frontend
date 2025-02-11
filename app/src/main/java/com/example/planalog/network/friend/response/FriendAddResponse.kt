package com.example.planalog.network.friend.response


data class FriendAddResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: FriendAddSuccess?
)

data class FriendAddSuccess(
    val message: String,
    val data: FriendAddResult
)

data class FriendAddResult(
    val id: Int,
    val toUserId: Int,
    val fromUserId: Int,
    val createdAt: String,
    val isAccepted: Boolean
)
