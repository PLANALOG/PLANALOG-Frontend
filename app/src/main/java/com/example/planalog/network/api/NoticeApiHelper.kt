package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.notice.NoticeAddRequest
import com.example.planalog.network.notice.NoticeAddResponse
import com.example.planalog.network.notice.NoticeDeleteResponse
import com.example.planalog.network.notice.NoticeGetResponse
import com.example.planalog.network.notice.NoticeService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeApiHelper(private val context : Context) {
    private val noticeService = RetrofitClient.create(NoticeService::class.java, context)

    fun addNotification(message : String, entityType : String, entityId: Int, onSuccess: (noticeId : String) -> Unit, onFailure: (String) -> Unit) {
        val request = NoticeAddRequest(message, entityType, entityId)

        noticeService.addNotice(request).enqueue(object : Callback<NoticeAddResponse> {
            override fun onResponse(p0: Call<NoticeAddResponse>, response: Response<NoticeAddResponse>) {
                if (response.isSuccessful) {
                    val noticeId = response.body()?.success?.data?.id
                    Log.d("NoticeApiHelper", "서버 알림 생성 성공 : ${noticeId}")
                    onSuccess(noticeId.toString())
                } else {
                    Log.e("NoticeApiHelper", "서버 알림 생성 실패: ${response.errorBody()?.string()}")
                    onFailure("서버 알림 생성 실패")
                }
            }

            override fun onFailure(p0: Call<NoticeAddResponse>, p1: Throwable) {
                Log.e("NoticeApiHelper", "네트워크 오류: ${p1.message.toString()}")
            }

        })
    }

    fun deleteNotification(noticeId : String) {

        noticeService.deleteNotice(noticeId).enqueue(object : Callback<NoticeDeleteResponse> {
            override fun onResponse(
                p0: Call<NoticeDeleteResponse>,
                response: Response<NoticeDeleteResponse>
            ) {
                if (response.isSuccessful) {
                    val deleteMessage = response.body()?.success?.message.toString()
                    Log.d("NoticeApiHelper", "서버 알림 삭제 성공: ${deleteMessage}")
                } else {
                    Log.e("NoticeApiHelper", "서버 알림 삭제 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(p0: Call<NoticeDeleteResponse>, p1: Throwable) {
                Log.e("NoticeApiHelper", "네트워크 오류: ${p1.message.toString()}")
            }

        })
    }

    fun getNotices() {

        noticeService.getNotice().enqueue(object : Callback<NoticeGetResponse> {
            override fun onResponse(p0: Call<NoticeGetResponse>, response: Response<NoticeGetResponse>) {
                if (response.isSuccessful) {
                    val success = response.body()?.success?.data
                    Log.d("NoticeApiHelper", "알림 목록 조회 성공: ${success}")
                } else {
                    Log.e("NoticeApiHelper", "알림 목록 조회 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(p0: Call<NoticeGetResponse>, p1: Throwable) {
                Log.e("NoticeApiHelper", "네트워크 오류: ${p1.message.toString()}")
            }

        })
    }
}