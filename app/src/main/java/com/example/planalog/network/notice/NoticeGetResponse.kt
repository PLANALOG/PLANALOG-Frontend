package com.example.planalog.network.notice

data class NoticeGetResponse (
    val resultType: String,
    val error: String?,
    val success: SuccessGetData?
)

data class SuccessGetData(
    val message: String,
    val data: List<NotificationItem>
)

data class NotificationItem(
    val id: Int,
    val isRead: Boolean,
    val message: String,
    val entityType: String,
    val entityId: Int,
    val createdAt: String
)