package com.example.planalog.network.user.request

data class UserUpdateRequest(
    val nickname: String? = null,
    val type: String? = null,
    val introduction: String? = null,
    val link: String? = null
) {
    companion object {
        fun fromMap(data: Map<String, Any>): UserUpdateRequest {
            return UserUpdateRequest(
                nickname = data["nickname"] as? String,
                type = data["type"] as? String,
                introduction = data["introduction"] as? String,
                link = data["link"] as? String
            )
        }
    }
}