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
    val momentId: Int, //반환되지 않음
    val title: String,
    val userName: String,
    val date: String, //반환되지 않음
    val likingCount: Int,
    val commentCount: Int,
    val thumbnailURL: String
)