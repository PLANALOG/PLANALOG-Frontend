package com.example.planalog.network.comment

data class CommentResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: SuccessResponse?
)

data class ErrorResponse(
    val errorCode: String,
    val reason: String
)

data class SuccessResponse(
    val id: Int,
    val content: String,
    val createdAt: String,
    val userId: Int,
    val momentId: Int
)