package com.example.planalog.network.post

data class MomentResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: SuccessData?,
    val message: String? = null
)

data class ErrorResponse(
    val errorCode: String,
    val reason: String,
    val data: Any? = null
)

data class SuccessData(
    val data: MomentDetails?
)

data class MomentDetails(
    val userId: Int,
    val momentId: Int,
    val plannerId: Int,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val momentContents: List<MomentContentResponse>
)

data class MomentContentResponse(
    val sortOrder: Int,
    val content: String,
    val url: String
)
