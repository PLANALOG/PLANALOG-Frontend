package com.example.planalog.network.task_category.response

import com.google.gson.annotations.SerializedName

data class GetCtgyResponse(
    @SerializedName("resultType") val resultType: String,
    @SerializedName("error") val error: Any?, // 에러가 null일 수 있음
    @SerializedName("success") val success: List<GetCategoryItem>
)

data class GetCategoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)
