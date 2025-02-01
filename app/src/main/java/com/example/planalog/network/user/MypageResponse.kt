package com.example.planalog.network.user

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
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val thumbnailUrl: String
)