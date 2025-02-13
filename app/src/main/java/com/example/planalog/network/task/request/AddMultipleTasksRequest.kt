package com.example.planalog.network.task.request

import com.google.gson.annotations.SerializedName

data class AddMultipleTasksRequest(
    @SerializedName("titles") val titles: List<String>,
    @SerializedName("planner_date") val plannerDate: String
)