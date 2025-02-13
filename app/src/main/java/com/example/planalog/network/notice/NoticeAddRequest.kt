package com.example.planalog.network.notice

data class NoticeAddRequest(
    val message : String,
    val entityType : String,
    val entityId : Int,
)
