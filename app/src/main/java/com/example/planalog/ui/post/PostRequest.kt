package com.example.planalog.ui.post

data class PostRequest(
    val title: String,
    val status: String,
    val plannerId: Int,
    val postContents: List<PostContent>
)

data class PostContent(
    val sortOrder: Int,
    val content: String,
    val url: String
)