package com.example.planalog.network.friend

import com.example.planalog.network.friend.request.FriendRequest
import com.example.planalog.network.friend.request.FriendRequestResponse
import com.example.planalog.network.friend.response.FriendDeleteResponse
import com.example.planalog.network.friend.response.FriendResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FriendApiService {
    @POST("/friends")
    fun sendFriendRequest(@Body request: FriendRequest): Call<FriendRequestResponse>

    @GET("/friends/following")
    fun getFollowing(): Call<FriendResponse>

    @GET("/friends/followers")
    fun getFollowers(): Call<FriendResponse>

    @DELETE("/friends/{friendId}")
    fun deleteFollowers(@Path("friendId") friendId: String): Call<FriendDeleteResponse>
}

