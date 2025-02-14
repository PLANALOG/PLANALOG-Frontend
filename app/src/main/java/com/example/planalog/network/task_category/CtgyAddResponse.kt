package com.example.planalog.network.task_category

import com.google.gson.annotations.SerializedName

data class CtgyAddResponse (
    @SerializedName("resultType") val resultType: String,
    @SerializedName("error") val error: Any?,
    @SerializedName("success") val success: SuccessCtgyResponse?
)

data class SuccessCtgyResponse(
    @SerializedName("resultType") val resultType: String,
    @SerializedName("error") val error: Any?,
    @SerializedName("data") val data: ResponseCtgyData
)

data class ResponseCtgyData(
    @SerializedName("success") val success: List<CtgyItem>,
    @SerializedName("failed") val failed: List<Any> // 실패 항목이 어떤 데이터인지 몰라서 Any 타입으로 설정
)

data class CtgyItem(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)