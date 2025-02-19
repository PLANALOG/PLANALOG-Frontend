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
    val id: Int,
    val userId: Int,
    val title: String,
    val plannerId: Int,
    val createdAt: String,
    val updatedAt: String,
    val momentContents: List<MomentContent>
)

data class MomentContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)
