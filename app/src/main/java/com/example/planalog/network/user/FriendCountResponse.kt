package com.example.planalog.network.user

data class FriendCountResponse(
    val resultType: String,
    val error: String?,
    val success: SuccessData
) {
    data class SuccessData(
        val message: String,
        val data: FriendCountData
    )
    data class FriendCountData(
        val friendCount: Int
    )
}