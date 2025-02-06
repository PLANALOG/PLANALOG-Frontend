package com.example.planalog.network.task.response

data class GetTasksResponse(
    val resultType: String,
    val error: String?,
    val success: TaskInfo?
)

data class TaskInfo(
    val id: Int,
    val plannerId: Int,
    val taskCategoryId: Int?,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)