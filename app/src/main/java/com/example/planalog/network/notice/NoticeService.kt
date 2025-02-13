package com.example.planalog.network.notice

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NoticeService {

    //알림 생성
    @POST("/notices")
    fun addNotice(
        @Body request: NoticeAddRequest
    ) : Call<NoticeAddResponse>

    @DELETE("/notices/{noticeId}")
    fun deleteNotice(
        @Path ("noticeId") noticeId : String
    ) : Call<NoticeDeleteResponse>

    @GET("/notices")
    fun getNotice() : Call<NoticeGetResponse>
}