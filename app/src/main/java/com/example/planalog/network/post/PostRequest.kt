package com.example.planalog.network.post

data class PostRequest(
    val title: String,
    val status: String,
    val plannerId: Int,
    val momentContents: List<PostContent>
)

data class PostContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)