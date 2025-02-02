package com.example.planalog.network.task

data class AddTaskResponse(
    val resultType: String,
    val error: String?,
    val success: TaskSuccess?
)

data class TaskSuccess(
    val id: Int,  // 생성된 할일 ID
    val plannerId: Int,
    val taskCategoryId: Int,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)

