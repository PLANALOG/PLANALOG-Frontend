package com.example.planalog.network.friend.request

import com.example.planalog.network.friend.response.ErrorResponse

data class FriendRequest(
    val toUserId: String
)

data class FriendRequestResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: FriendRequestSuccess?
)

data class FriendRequestSuccess(
    val message: String,
    val data: FriendRequestResult
)

data class FriendRequestResult(
    val id: Int,
    val toUserId: Int,
    val fromUserId: Int,
    val createdAt: String,
    val isAccepted: Boolean
)