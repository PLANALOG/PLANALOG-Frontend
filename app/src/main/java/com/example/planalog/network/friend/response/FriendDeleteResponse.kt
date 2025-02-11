package com.example.planalog.network.friend.response

data class FriendDeleteResponse(
    val resultType: String,
    val error: String?,
    val success: FriendDeleteData?
)

data class FriendDeleteData(
    val data : List<FriendDeleteList>
)

data class FriendDeleteList(
    val id : Int,
    val toUserId : Int,
    val fromUserID : Int,
    val createdAt : String,
    val isAccepted : Boolean,
)