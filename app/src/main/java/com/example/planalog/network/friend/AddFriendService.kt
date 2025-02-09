package com.example.planalog.network.friend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class FriendRequest(
    val toUserId: String
)

data class FriendRequestResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: FriendRequestSuccess?
)

data class FriendRequestSuccess(
    val message: String,
    val result: FriendRequestResult
)

data class FriendRequestResult(
    val id: Int,
    val toUserId: Int,
    val fromUserId: Int,
    val createdAt: String,
    val isAccepted: Boolean
)



interface AddFriendService {
    @POST("/friends")
    fun sendFriendRequest(@Body request: FriendRequest): Call<FriendRequestResponse>
}
