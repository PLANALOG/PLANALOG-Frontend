package com.example.planalog.network.friend.response


data class FriendpageResponse(
    val resultType: String,
    val error: String?,
    val success: FriendpageData?
)

data class FriendpageData(
    val data: List<FriendpageMoment>
)

data class FriendpageMoment(
    val momentId: Int,
    val title: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val thumbnailUrl: String
)
