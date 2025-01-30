package com.example.planalog.ui.post

import android.net.Uri

data class Slide(
    val imageResId: Uri?,
    var postContent: String = ""  // 슬라이드별 텍스트 추가
)
