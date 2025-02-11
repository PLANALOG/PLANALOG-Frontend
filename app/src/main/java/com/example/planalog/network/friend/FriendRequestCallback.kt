package com.example.planalog.network.friend

interface FriendRequestCallback {
    fun onRequestSuccess(friendId: String?)
    fun onRequestFailure(message: String)
}
