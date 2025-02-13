package com.example.planalog.network.friend.response

data class FriendAcceptResponse(
    val resultType: String,
    val error: String?,
    val success: SuccessResponse?
)

data class SuccessResponse(
    val message: String,
    val data: FriendRequestData
)

data class FriendRequestData(
    val id: Int,
    val toUserId: Int,
    val fromUserId: Int,
    val createdAt: String,
    val isAccepted: Boolean
)
