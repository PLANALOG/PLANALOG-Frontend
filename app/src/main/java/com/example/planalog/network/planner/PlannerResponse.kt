package com.example.planalog.network.planner

data class PlannerResponse(
    val resultType: String,
    val error: Any?,
    val success: PlannerSuccessResponse
)

data class PlannerSuccessResponse(
    val plannerId: Int,
    val userId: Int,
    val isCompleted: Boolean,
    val tasks: List<Task>
)

data class Task(
    val taskId: Int,
    val title: String,
    val isCompleted: Boolean,
    val taskCategoryId: Int,
    val taskCategoryName: String
)