package com.example.planalog.ui.post

data class PostResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: SuccessResponse?,
    val message: String?
)

data class ErrorResponse(
    val errorCode: String,
    val reason: String,
    val data: Any?
)

data class SuccessResponse(
    val data: PostData?
)

data class PostData(
    val userId: Int,
    val postId: Int,
    val plannerId: Int,
    val title: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val postContents: List<PostContent>
)