package com.example.planalog.network.user

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Path

interface FriendcountService {
    @GET("/friends/count")
    fun getFriendCount(): Call<FriendCountResponse>

    @GET("/friends/count/{userId}")
    fun getUserFriendCount(@Path("userId") userId: Int): Call<FriendCountResponse>
}