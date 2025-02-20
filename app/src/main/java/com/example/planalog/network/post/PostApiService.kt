package com.example.planalog.network.post

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface PostApiService {
    @POST("/moments")
    fun createMoment(@Body requestBody: MomentRequest): Call<MomentResponse>

    @DELETE("/moments/{momentId}")
    fun deleteMoment(@Path("momentId") momentId: Int): Call<Void>
}
