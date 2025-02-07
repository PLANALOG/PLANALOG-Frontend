package com.example.planalog.network.friend

import retrofit2.Call
import retrofit2.http.GET

data class FriendResponse(
    val resultType: String,
    val success: List<Friend>?,
    val error: ErrorResponse?
)

data class Friend(
    val friendId: Int,
    val id: Int,
    val name: String,
    val email: String,
    val nickname: String,
    val introduction: String,
    val link: String
)

data class ErrorResponse(
    val errorCode: String,
    val reason: String
)

interface FriendApiService {
    @GET("/friends/following")
    fun getFollowing(): Call<FriendResponse>

    @GET("/friends/followers")
    fun getFollowers(): Call<FriendResponse>
}

