package com.example.planalog.network.user

import com.example.planalog.network.user.response.MypageResponse
import retrofit2.Call
import retrofit2.http.GET

interface MypageService {
    @GET("/moments/mine")
    fun getMypageMoments(): Call<MypageResponse>
}