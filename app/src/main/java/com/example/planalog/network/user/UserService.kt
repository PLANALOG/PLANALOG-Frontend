package com.example.planalog.network.user

import com.example.planalog.network.user.UserUpdateRequest
import com.example.planalog.network.user.UserUpdateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UserService {
    @PATCH("/users/profile")
    fun updateUser(
        @Body request: UserUpdateRequest,
    ): Call<UserUpdateResponse>

    @GET("/users")
    fun getUserInfo(): Call<UserResponse>
}