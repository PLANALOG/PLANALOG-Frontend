package com.example.planalog.network.friend

import com.example.planalog.network.friend.request.FriendAddRequest
import com.example.planalog.network.friend.response.FriendAcceptResponse
import com.example.planalog.network.friend.response.FriendAddResponse
import com.example.planalog.network.friend.response.FriendDeleteResponse
import com.example.planalog.network.friend.response.FriendResponse
import com.example.planalog.network.friend.response.FriendpageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FriendService {

    @POST("/friends")
    fun sendFriendRequest(@Body request: FriendAddRequest): Call<FriendAddResponse>

    @GET("/friends/following")
    fun getFollowing(): Call<FriendResponse>

    @GET("/friends/followers")
    fun getFollowers(): Call<FriendResponse>

    @DELETE("/friends/{friendId}")
    fun deleteFollowers(@Path("friendId") friendId: String): Call<FriendDeleteResponse>

    @GET("/friends/{friendId}/moments")
    fun getFriendpageMoments(): Call<FriendpageResponse>

    @PATCH("/friends/{friendId}")
    fun acceptFriend(
        @Path("friendId") friendId: String
    ) : Call<FriendAcceptResponse>
}
