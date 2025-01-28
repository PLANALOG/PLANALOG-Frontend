package com.example.planalog.ui.post

import android.net.Uri

data class Slide(
    val imageResId: Uri?,   // 이미지 주소
    var postContent: String // 게시물 내용
)