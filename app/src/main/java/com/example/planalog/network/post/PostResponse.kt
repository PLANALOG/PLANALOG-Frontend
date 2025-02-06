package com.example.planalog.network.post

data class PostResponse(
    val resultType: String,
    val error: String?,
    val success: SuccessData?
)

data class SuccessData(
    val data: MomentData
)

data class MomentData(
    val userId: Int,
    val momentId: Int,
    val plannerId: Int,
    val title: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val momentContents: List<PostContent>
)