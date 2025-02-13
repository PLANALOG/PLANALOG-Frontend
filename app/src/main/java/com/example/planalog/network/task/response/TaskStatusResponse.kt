package com.example.planalog.network.task.response

data class TaskStatusResponse(
    val resultType: String,
    val error: Any?,
    val success: SuccessStatus?
)

data class SuccessStatus(
    val id: Int,
    val plannerId: Int,
    val taskCategoryId: Int?,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val message: String
)