package com.example.planalog.network.user.response

data class MypageResponse(
    val resultType: String,
    val error: String?,
    val success: MypageData?
)

data class MypageData(
    val data: List<MypageMoment>
)

data class MypageMoment(
    val momentId: Int,
    val title: String,
    val userName: String,
    val date: String,
    val likingCount: Int,
    val commentCount: Int,
    val thumbnailUrl: String
)