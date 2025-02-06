package com.example.planalog.network.task.request

data class AddMultipleTasksRequest(
    val title: List<String>,
    val planner_date: String
)
