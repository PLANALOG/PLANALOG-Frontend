package com.example.planalog.network.task_category.request

data class AddCtgyTaskRequest(
    val titles: List<String>,
    val planner_date: String
)