package com.example.planalog.network.user.response

data class UserProfileImgResponse(
    val resultType: String,
    val error: String?,
    val success: UserProfileImg?
)

data class UserProfileImg(
    var message: String,
    var savedUrl: String,
)