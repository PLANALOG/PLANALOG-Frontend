package com.example.planalog.network.notice

data class NoticeDeleteResponse(
    val resultType: String,
    val error: String?,
    val success: SuccessMessage?
)

data class SuccessMessage(
    val message: String
)
