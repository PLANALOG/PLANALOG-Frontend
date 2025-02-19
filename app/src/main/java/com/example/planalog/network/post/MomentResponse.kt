package com.example.planalog.network.post

data class MomentResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: SuccessResponse?
)

data class ErrorResponse(
    val errorCode: String,
    val reason: String,
    val data: Any?
)

data class SuccessResponse(
    val data: MomentData
)

data class MomentData(
    val momentId: Int, // 기존 id에서 변경됨
    val userId: Int,
    val title: String,
    val plannerId: Int, // Long이 아닌 Int로 변경 필요
    val date: String?, // 새 필드 추가
    val createdAt: String,
    val updatedAt: String,
    val momentContents: List<MomentContent>
)

data class MomentContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)
