package com.example.planalog.network.user.response

import com.example.planalog.network.friend.response.ErrorResponse

data class FriendProfileResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: FriendProfile?  // success가 FriendProfile 객체를 직접 포함
)

data class FriendProfile(
    val userId: Int,
    val nickname: String,
    val type: String,
    val introduction: String,
    val link: String,
    val profileImage: String?
)