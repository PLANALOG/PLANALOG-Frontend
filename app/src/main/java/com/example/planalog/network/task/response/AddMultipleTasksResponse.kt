package com.example.planalog.network.task.response

data class AddMultipleTasksResponse(
    val resultType: String,
    val error: String?,
    val success: List<TodoItem>?
)

data class TodoItem(
    val id: Int,
    val title: String,
    val plannerDate: String?
)
