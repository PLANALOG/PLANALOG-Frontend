package com.example.planalog.ui.post

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PostApiService {
    @POST("/moments")
    fun createPost(@Body requestBody: PostRequest): Call<PostResponse>
}