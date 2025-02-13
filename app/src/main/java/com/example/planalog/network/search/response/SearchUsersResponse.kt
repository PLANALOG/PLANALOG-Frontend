package com.example.planalog.network.search.response

data class SearchUsersResponse(
    val resultType: String,
    val error: String?,
    val success: Success?
)

data class Success(
    val message: String,
    val data: List<User>
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val introduction: String,
    val link: String,
    val nickname: String
)
