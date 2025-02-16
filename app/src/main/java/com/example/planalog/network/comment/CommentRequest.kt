package com.example.planalog.network.comment

data class CommentRequest(
    val comment: CommentContent
)

data class CommentContent(
    val content: String
)
