package com.example.planalog.network.planner

data class PlannerCalendarResponse(
    val resultType: String,
    val error: String?,
    val success: PlannerSuccess?
)

data class PlannerSuccess(
    val startDate: String,
    val endDate: String,
    val planners: List<PlannerCalendar>
)

data class PlannerCalendar(
    val plannerId: Int,
    val plannerDate: String,
    val isCompleted: Boolean
)