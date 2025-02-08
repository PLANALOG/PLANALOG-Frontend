package com.example.planalog.network.friend

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class FriendProfileResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: FriendProfile?  // success가 FriendProfile 객체를 직접 포함
)

data class FriendProfile(
    val userId: String,
    val nickname: String,
    val type: String,
    val introduction: String,
    val link: String,
    val createdAt: String,
    val updatedAt: String
)

interface FriendDataService {
    @GET("user/{userId}")
    fun getFriendProfile(@Path("userId") userId: String): Call<FriendProfileResponse>
}
