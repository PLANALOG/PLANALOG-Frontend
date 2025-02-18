package com.example.planalog.network.task_category.response

import com.google.gson.annotations.SerializedName

data class CtgyDeleteResponse(
    @SerializedName("resultType") val resultType: String,
    @SerializedName("error") val error: Any?,
    @SerializedName("success") val success: DeleteSuccessCtgyMessage?
)

data class DeleteSuccessCtgyMessage(
    @SerializedName("message") val message: String,
    @SerializedName("count") val count: Int
)
