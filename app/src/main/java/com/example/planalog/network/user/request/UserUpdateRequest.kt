package com.example.planalog.network.user.request

data class UserUpdateRequest(
    val nickname: String,
    val type: String,
    val introduction: String? = "Hello, I am new here!",
    val link: String? = "https://myportfolio.com"
)