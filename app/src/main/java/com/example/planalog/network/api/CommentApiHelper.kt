package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.post.*
import com.example.planalog.network.comment.*
import com.example.planalog.network.comment.SuccessResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentApiHelper(private val context: Context) {

    private val commentApiService: CommentApiService = RetrofitClient.create(CommentApiService::class.java, context)
    private val postApiService: PostApiService = RetrofitClient.create(PostApiService::class.java, context)

    private var momentId: Int = -1  // momentId를 내부 변수로 저장

    // momentId를 서버에서 가져오는 함수 (Call 사용)
    fun fetchMomentId(requestBody: MomentRequest, callback: MomentIdCallback) {
        postApiService.createMoment(requestBody).enqueue(object : Callback<MomentResponse> {
            override fun onResponse(call: Call<MomentResponse>, response: Response<MomentResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val momentData = response.body()?.success?.data
                    if (momentData != null) {
                        momentId = momentData.id
                        Log.d("CommentApiHelper", "Fetched momentId: $momentId") // 로그 추가
                        callback.onSuccess(momentId)
                    } else {
                        Log.e("CommentApiHelper", "Failed to extract momentId")
                        callback.onFailure("momentId를 찾을 수 없습니다.")
                    }
                } else {
                    Log.e("CommentApiHelper", "Server Response Failed: ${response.code()} - ${response.message()}")
                    callback.onFailure("서버 응답 실패: ${response.code()} - ${response.message()}")
                }
            }


            override fun onFailure(call: Call<MomentResponse>, t: Throwable) {
                Log.e("API_CALL", "Network Error fetching momentId: ${t.localizedMessage}")
                callback.onFailure("네트워크 오류: ${t.localizedMessage}")
            }
        })
    }

    // 댓글 등록 버튼 클릭 시 API 요청 처리
    fun handleCommentPostButtonClick(content: String, callback: CommentCallback) {
        Log.d("CommentApiHelper", "handleCommentPostButtonClick called with content: $content")

        // ✅ SharedPreferences에서 momentId 가져오기
        momentId = getMomentIdFromSPF()

        if (momentId == -1) {
            Log.d("CommentApiHelper", "momentId가 없습니다. momentId를 먼저 가져옵니다.")

            val requestBody = MomentRequest(
                title = "새로운 순간",
                plannerId = 123,
                momentContents = listOf(
                    MomentRequestContent(sortOrder = 1, content = "이 순간을 기록합니다!", url = "")
                )
            )

            fetchMomentId(requestBody, object : MomentIdCallback {
                override fun onSuccess(fetchedMomentId: Int) {
                    momentId = fetchedMomentId
                    saveMomentIdToSPF(momentId) // ✅ 가져온 momentId를 SharedPreferences에 저장
                    Log.d("CommentApiHelper", "momentId 가져오기 성공: $momentId")

                    // postComment를 올바르게 호출 (callback 포함)
                    CoroutineScope(Dispatchers.Main).launch {
                        postComment(momentId, content, callback)
                    }
                }

                override fun onFailure(errorMessage: String) {
                    Log.e("CommentApiHelper", "momentId 가져오기 실패: $errorMessage")
                    Toast.makeText(context, "momentId 가져오기 실패: $errorMessage", Toast.LENGTH_SHORT).show()
                    callback.onFailure("momentId 가져오기 실패: $errorMessage")
                }
            })
            return
        }

        if (content.isEmpty()) {
            Log.e("CommentApiHelper", "댓글을 입력해주세요.")
            Toast.makeText(context, "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show()
            callback.onFailure("댓글을 입력해주세요.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            Log.d("CommentApiHelper", "Calling postComment() with momentId: $momentId")
            postComment(momentId, content, callback)
        }
    }


    private fun getMomentIdFromSPF(): Int {
        val sharedPreferences = context.getSharedPreferences("MomentData", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("MOMENT_ID", -1) // 기본값 -1 (저장된 값이 없을 경우)
    }

    private fun saveMomentIdToSPF(momentId: Int) {
        val sharedPreferences = context.getSharedPreferences("MomentData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("MOMENT_ID", momentId)
        editor.apply()
        Log.d("CommentApiHelper", "momentId 저장 완료: $momentId")
    }


    // momentId를 사용하여 댓글을 서버로 전송
    private suspend fun postComment(momentId: Int, content: String, callback: CommentCallback) {
        try {
            Log.d("CommentApiHelper", "🔵 postComment() 호출됨 - momentId: $momentId, content: $content") // ✅ 로그 추가

            val request = CommentRequest(CommentContent(content))
            Log.d("CommentApiHelper", "🔵 요청 데이터: $request") // ✅ 서버로 가는 데이터 확인

            val response = withContext(Dispatchers.IO) {
                commentApiService.postComment(momentId, request)
            }

            Log.d("CommentApiHelper", "🟢 서버 응답 코드: ${response.code()}") // ✅ 서버 응답 코드 로그

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()
                Log.d("CommentApiHelper", "🟢 서버 응답 데이터: $responseBody") // ✅ 응답 데이터 로그 추가

                val successData = responseBody?.success

                if (successData is Int) {
                    Log.d("CommentApiHelper", "🟢 댓글 ID: $successData")
                    callback.onSuccess(SuccessResponse(id = successData, content = content, createdAt = "", userId = 0, momentId = momentId))
                } else {
                    Log.e("CommentApiHelper", "🔴 Unexpected success format: $successData")
                    callback.onFailure("서버 응답 형식이 올바르지 않습니다.")
                }
            } else {
                Log.e("CommentApiHelper", "🔴 서버 응답 실패: ${response.message()}")
                callback.onFailure("Failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("CommentApiHelper", "🔴 Network error: ${e.localizedMessage}")
            callback.onFailure("Network error: ${e.localizedMessage}")
        }
    }

    // 콜백 인터페이스 정의
    interface MomentIdCallback {
        fun onSuccess(momentId: Int)
        fun onFailure(errorMessage: String)
    }

    interface CommentCallback {
        fun onSuccess(comment: SuccessResponse)
        fun onFailure(errorMessage: String)
    }
}
