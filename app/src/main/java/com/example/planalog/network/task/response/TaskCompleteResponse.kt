package com.example.planalog.network.task.response

data class TaskCompleteResponse(
    val resultType: String,
    val error: String?,
    val success: List<TaskComplete>?
)

data class TaskComplete(
    val id: Int,
    val plannerId: Int,
    val taskCategoryId: Int?,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)