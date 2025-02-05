package com.example.planalog.network.task.response

data class DeleteTasksResponse(
    val resultType: String,
    val error: String?,  // 에러가 없을 때는 null이므로 nullable로 처리
    val success: SuccessResponse?
)

data class SuccessResponse(
    val deletedTasks: List<DeletedTask>
)

data class DeletedTask(
    val id: Int,
    val plannerId: Int,
    val taskCategoryId: Int?,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)
