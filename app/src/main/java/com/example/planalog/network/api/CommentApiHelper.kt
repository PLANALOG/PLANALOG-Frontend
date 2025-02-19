//package com.example.planalog.network.api
//
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import com.example.planalog.network.RetrofitClient
//import com.example.planalog.network.post.*
//import com.example.planalog.network.comment.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class CommentApiHelper(private val context: Context) {
//
//    private val commentApiService: CommentApiService = RetrofitClient.create(CommentApiService::class.java, context)
//    private val postApiService: PostApiService = RetrofitClient.create(PostApiService::class.java, context)
//
//    private var momentId: Int = -1  // momentId를 내부 변수로 저장
//
//    // momentId를 서버에서 가져오는 함수 (Call 사용)
//    fun fetchMomentId(requestBody: MomentRequest, callback: MomentIdCallback) {
//        postApiService.createMoment(requestBody).enqueue(object : Callback<MomentResponse> {
//            override fun onResponse(call: Call<MomentResponse>, response: Response<MomentResponse>) {
//                if (response.isSuccessful && response.body() != null) {
//                    val momentData = response.body()?.success?.data
//                    if (momentData != null) {
//                        momentId = momentData.id
//                        Log.d("API_CALL", "Fetched momentId: $momentId")
//                        callback.onSuccess(momentId)
//                    } else {
//                        Log.e("API_CALL", "Failed to extract momentId")
//                        callback.onFailure("momentId를 찾을 수 없습니다.")
//                    }
//                } else {
//                    Log.e("API_CALL", "Server Response Failed: ${response.code()} - ${response.message()}")
//                    callback.onFailure("서버 응답 실패: ${response.code()} - ${response.message()}")
//                }
//            }
//
//            override fun onFailure(call: Call<MomentResponse>, t: Throwable) {
//                Log.e("API_CALL", "Network Error fetching momentId: ${t.localizedMessage}")
//                callback.onFailure("네트워크 오류: ${t.localizedMessage}")
//            }
//        })
//    }
//
//
//    // 댓글 등록 버튼 클릭 시 API 요청 처리
//    fun handleCommentPostButtonClick(content: String, callback: CommentCallback) {
//        if (momentId == -1) {
//            Toast.makeText(context, "momentId가 없습니다.", Toast.LENGTH_SHORT).show()
//            callback.onFailure("momentId가 없습니다.")
//            return
//        }
//
//        if (content.isEmpty()) {
//            Toast.makeText(context, "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show()
//            callback.onFailure("댓글을 입력해주세요.")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            postComment(momentId, content, callback)
//        }
//    }
//
//    // momentId를 사용하여 댓글을 서버로 전송
//    private suspend fun postComment(momentId: Int, content: String, callback: CommentCallback) {
//        try {
//            val response = withContext(Dispatchers.IO) {
//                commentApiService.postComment(momentId, CommentRequest(CommentContent(content)))
//            }
//
//            if (response.isSuccessful && response.body() != null) {
//                callback.onSuccess(response.body()!!.success!!)
//            } else {
//                callback.onFailure("Failed: ${response.message()}")
//            }
//        } catch (e: Exception) {
//            callback.onFailure("Network error: ${e.localizedMessage}")
//        }
//    }
//
//    // 콜백 인터페이스 정의
//    interface MomentIdCallback {
//        fun onSuccess(momentId: Int)
//        fun onFailure(errorMessage: String)
//    }
//
//    interface CommentCallback {
//        fun onSuccess(comment: SuccessResponse)
//        fun onFailure(errorMessage: String)
//    }
//}
