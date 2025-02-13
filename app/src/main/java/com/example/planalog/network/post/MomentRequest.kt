package com.example.planalog.network.post

data class MomentRequest(
    val title: String,
    val plannerId: Int?,
    val momentContents: List<MomentRequestContent>
)

data class MomentRequestContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)
