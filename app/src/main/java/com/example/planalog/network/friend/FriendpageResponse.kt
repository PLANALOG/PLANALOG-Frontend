package com.example.planalog.network.friend

import com.example.planalog.network.user.response.MypageMoment

data class FriendpageResponse(
    val resultType: String,
    val error: String?,
    val success: FriendpageData?
)

data class FriendpageData(
    val data: List<MypageMoment>
)

data class FriendpageMoment(
    val momentId: Int,
    val title: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val thumbnailUrl: String
)
