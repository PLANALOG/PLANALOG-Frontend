package com.example.planalog.ui.comment.com.example.planalog.ui.comment

import com.example.planalog.R

data class Comment(
    val userName: String,
    val content: String,
    val profileImage: Int = R.drawable.ic_profile // 기본 프로필 이미지
)
