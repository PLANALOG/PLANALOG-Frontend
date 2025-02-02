package com.example.planalog.network.user

import retrofit2.http.GET
import retrofit2.Call

interface FriendcountService {
    @GET("/friends/count")
    fun getFriendCount(): Call<FriendCountResponse>
}