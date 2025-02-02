package com.example.planalog.network.post

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PostApiService {
    @POST("/moments")
    fun createPost(@Body requestBody: PostRequest): Call<PostResponse>
}