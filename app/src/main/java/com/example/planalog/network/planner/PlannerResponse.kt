package com.example.planalog.network.planner

data class PlannerResponse(
    val resultType: String,
    val error: String?,
    val success: PlannerItem?
)

data class PlannerItem(
    val date: String,
    val isCompleted: Boolean
)