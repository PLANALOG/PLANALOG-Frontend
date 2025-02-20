package com.example.planalog.network.post

data class UserPageMomentResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: UserPageSuccessResponse?
)

data class UserPageSuccessResponse(
    val data: UserPageMomentData
)

data class UserPageMomentData(
    val userId: Int,
    val title: String,
    val date: String,
    val plannerId: Int?,
    val createdAt: String,
    val updatedAt: String,
    val momentContents: List<UserPageMomentContent>
)

data class UserPageMomentContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)
