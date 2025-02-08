package com.example.planalog.network.post

data class MomentRequest(
    val title: String,
    val plannerId: Int,
    val momentContents: List<MomentContent>
)

data class MomentContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)
