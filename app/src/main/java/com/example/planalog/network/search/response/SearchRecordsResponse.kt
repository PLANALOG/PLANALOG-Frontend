package com.example.planalog.network.search.response

data class SearchRecordsResponse(
    val resultType: String,
    val error: String?,
    val success: RecordsSuccess?
)

data class RecordsSuccess(
    val message: String,
    val data: List<Records>
)

data class Records(
    val id: Int,
    val content: String,
    val createdAt: String,
)