package com.example.planalog.network.post

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PostApiService {
    @POST("/moments")
    suspend fun createMoment(@Body requestBody: MomentRequest): Response<MomentResponse>
}
