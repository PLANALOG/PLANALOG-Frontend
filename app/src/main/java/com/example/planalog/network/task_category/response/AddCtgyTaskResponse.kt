package com.example.planalog.network.task_category.response

data class AddCtgyTaskResponse(
    val resultType: String,
    val error: String?,
    val success: List<TodoItem>
)

data class TodoItem(
    val id: Int,
    val plannerId: Int,
    val taskCategoryId: Int,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)

