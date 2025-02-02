package com.example.planalog.network.planner

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlannerService {

    //플래너 조회 api
    @GET("/planners")
    fun getPlanners(
        @Query("userId") userId: String? = null,  // 사용자 ID (옵션)
        @Query("date") date: String? = null,  // 조회할 날짜 (옵션)
        @Query("month") month: String? = null  // 조회할 월 (옵션)
    ) : Call<PlannerResponse>
}