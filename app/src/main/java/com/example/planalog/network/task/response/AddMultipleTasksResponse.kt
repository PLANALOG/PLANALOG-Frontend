package com.example.planalog.network.task.response

data class AddMultipleTasksResponse(
    val resultType: String,
    val error: String?,
    val success: List<AddedTask>  // 할 일 리스트
)

data class AddedTask(
    val title: String,
    val planner_date: String
)