package com.example.planalog.ui.post

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PostsApiService {
    @POST("/moments")
    fun createMoment(@Body request: PostRequest): Call<PostResponse>
}