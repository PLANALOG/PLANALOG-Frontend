package com.example.planalog.network.search.response

data class SearchDeleteResponse(
    val resultType: String,
    val error: String?,
    val success: DeleteSuccess?
)

data class DeleteSuccess(
    val message: String,
)