package com.example.planalog.network.post

data class MomentResponse(
    val resultType: String,
    val error: Any?,
    val success: SuccessData
)

data class SuccessData(
    val data: MomentData
)

data class MomentData(
    val id: Int,
    val userId: Int,
    val title: String,
    val plannerId: Long,
    val createdAt: String,
    val updatedAt: String,
    val momentContents: List<MomentContent>
)

data class MomentContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)