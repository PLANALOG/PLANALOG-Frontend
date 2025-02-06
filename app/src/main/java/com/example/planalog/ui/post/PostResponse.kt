package com.example.planalog.ui.post

data class PostResponse(
    val resultType: String,
    val error: ErrorData?,
    val success: SuccessData?,
    val message: String?
)

data class ErrorData(
    val errorCode: String?,
    val reason: String?,
    val data: Any?
)

data class SuccessData(
    val data: PostSuccessData?
)

data class PostSuccessData(
    val userId: Int,
    val postId: Int,
    val plannerId: Int,
    val title: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val postContents: List<PostContent>
)