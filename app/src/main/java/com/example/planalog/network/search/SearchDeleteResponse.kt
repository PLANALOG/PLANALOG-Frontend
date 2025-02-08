package com.example.planalog.network.search

data class SearchDeleteResponse(
    val resultType: String,
    val error: String?,
    val success: DeleteSuccess?
)

data class DeleteSuccess(
    val message: String,
)