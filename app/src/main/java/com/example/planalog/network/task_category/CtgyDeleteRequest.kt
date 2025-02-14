package com.example.planalog.network.task_category

import com.google.gson.annotations.SerializedName

data class CtgyDeleteRequest (
    @SerializedName("categoryIds") val categoryIds: List<Int>
    )