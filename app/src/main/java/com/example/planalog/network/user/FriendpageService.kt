package com.example.planalog.network.user

import retrofit2.Call
import retrofit2.http.GET

interface FriendpageService {
    @GET("/moments/friends/friendId}/{momentId}")
    fun getFriendpageMoments(): Call<FriendpageResponse>
}