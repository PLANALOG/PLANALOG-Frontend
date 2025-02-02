package com.example.planalog.network.startset

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface IdcheckService {
    @GET("/users/check_nickname")
    fun idcheck(
        @Query("nickname") nickname: String
    ): Call<NicknameCheckResponse>
}

