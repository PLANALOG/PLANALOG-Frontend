package com.example.planalog.network.user

import com.example.planalog.network.post.MomentResponse
import com.example.planalog.network.post.UserPageMomentResponse
import com.example.planalog.network.user.response.MypageResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MypageService {
    @GET("/mypage/moments")
    fun getMypageMoments(): Call<MypageResponse>

    // ✅ 추가: 특정 momentId의 상세 데이터를 가져오는 API
    @GET("/mypage/moments/{momentId}")
    fun getUserPageMomentDetail(@Path("momentId") momentId: Int): Call<UserPageMomentResponse>
}