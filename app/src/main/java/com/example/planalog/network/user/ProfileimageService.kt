package com.example.planalog.network.user

import okhttp3.RequestBody
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Query

interface ProfileimageService {
    @POST("/users/profile/image")
    fun uploadProfileImage(
        @Body image: RequestBody,
        @Query("basicImage") basicImage: Int? = null
    ): Call<Void>
}