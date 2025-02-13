package com.example.planalog.network.notice

data class NoticeAddResponse(
    val resultType: String,
    val error: String?,
    val success: SuccessData?
)

data class SuccessData(
    val message: String,
    val data: NotificationData
)

data class NotificationData(
    val id: Int,
    val userId: Int,
    val isRead: Boolean,
    val message: String,
    val entityType: String,
    val entityId: Int,
    val createdAt: String
)
