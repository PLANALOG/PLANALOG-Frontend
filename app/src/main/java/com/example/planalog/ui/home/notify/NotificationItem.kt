package com.example.planalog.ui.home.notify

data class NotificationItem(
    val userId: String,
    val noticeId : String,
    val profileImageRes: Int, // 프로필 이미지 리소스 ID
    val message: String,
    val onAccept: ((String) -> Unit)? = null,
    val onReject: ((String, String) -> Unit)? = null
)

