package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import com.example.planalog.R
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.notice.NoticeAddRequest
import com.example.planalog.network.notice.NoticeAddResponse
import com.example.planalog.network.notice.NoticeDeleteResponse
import com.example.planalog.network.notice.NoticeGetResponse
import com.example.planalog.network.notice.NoticeService
import com.example.planalog.network.notice.NotificationItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeApiHelper(private val context : Context) {
    private val noticeService = RetrofitClient.create(NoticeService::class.java, context)
    private val userApiHelper = UserApiHelper(context)

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

    fun getNotices(onResult: (List<NotificationItem>) -> Unit) {

        noticeService.getNotice().enqueue(object : Callback<NoticeGetResponse> {
            override fun onResponse(p0: Call<NoticeGetResponse>, response: Response<NoticeGetResponse>) {
                if (response.isSuccessful) {
                    val rawNotices = response.body()?.success?.data ?: listOf()
                    val processedNotices = mutableListOf<NotificationItem>()

                    rawNotices.forEach { notice ->
                        userApiHelper.getFriendProfile(notice.fromUserId) { nickname ->
                            val formattedMessage = "$nickname 님이 나를 응원하고 싶어합니다."
                            Log.d("NoticeApiHelper", "formattedMessage : $formattedMessage")

                            processedNotices.add(
                                NotificationItem(
                                    fromUserId = notice.fromUserId,
                                    fromUserName = nickname,  // 앱에서 설정한 닉네임 사용
                                    message = formattedMessage,
                                    id = notice.id,
                                    createdAt = notice.createdAt,
                                    entityType = notice.entityType,
                                    entityId = notice.entityId,
                                    isRead = notice.isRead,
                                    onAccept = { userId ->
                                        Log.d("NotificationAdapter", "알림 수락: $userId")
                                    },
                                    onReject = { userId, noticeId ->
                                        Log.d("NotificationAdapter", "알림 거절: $userId, ID: $noticeId")
                                        deleteNotification(noticeId)
                                    }
                                )
                            )

                            // 모든 데이터를 불러왔으면 UI 갱신
                            if (processedNotices.size == rawNotices.size) {
                                onResult(processedNotices)
                            }
                        }
                    }
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