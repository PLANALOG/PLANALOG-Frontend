package com.example.planalog.network.task.response

import com.google.gson.annotations.SerializedName

data class GetTasksResponse(
    @SerializedName("resultType") val resultType: String,
    @SerializedName("error") val error: Any?, // 에러가 null일 수 있음
    @SerializedName("success") val tasks: List<TaskInfo>
)

data class TaskInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("plannerId") val plannerId: Int,
    @SerializedName("taskCategoryId") val taskCategoryId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("isCompleted") val isCompleted: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)