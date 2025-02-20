package com.example.planalog.ui.home

data class HomeBlogItem(
    val userName: String,
    val blogDate: String,
    val blogDetail: String,
    val likeCount: Int,
    val profileImageRes: Int,
    val blogImageRes: Int,
    val likeImageRes: Int,
    val replyImageRes: Int
)