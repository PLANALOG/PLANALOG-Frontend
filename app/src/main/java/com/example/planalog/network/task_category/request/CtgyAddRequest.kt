package com.example.planalog.network.task_category.request

import com.google.gson.annotations.SerializedName

data class CtgyAddRequest(
    @SerializedName("names") val names: List<String>
)
