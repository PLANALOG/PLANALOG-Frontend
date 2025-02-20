package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.post.MomentRequest
import com.example.planalog.network.post.MomentRequestContent
import com.example.planalog.network.post.MomentResponse
import com.example.planalog.network.post.PostApiService
import com.example.planalog.network.post.UserPageMomentResponse
import com.example.planalog.network.user.MypageService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostApiHelper(private val context: Context) {

    private val postService = RetrofitClient.create(PostApiService::class.java, context)
    private val mypageService = RetrofitClient.create(MypageService::class.java, context)

    interface UploadPostCallback {
        fun onSuccess(postedId: Int?, momentId: Int?)
        fun onFailure(errorMessage: String)
    }
    interface UserPageMomentCallback {
        fun onSuccess(response: UserPageMomentResponse)
        fun onFailure(errorMessage: String)
    }

    fun uploadPost(title: String, plannerId: Int?, momentContents: List<MomentRequestContent>, callback: UploadPostCallback) {
        val request = MomentRequest(title, plannerId, momentContents)

        postService.createMoment(request).enqueue(object : Callback<MomentResponse> {
            override fun onResponse(call: Call<MomentResponse>, response: Response<MomentResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val momentData = response.body()?.success?.data
                    val momentId = momentData?.id
                    Log.d("PostApiHelper", "모먼트 생성 성공: $momentData")
                    Log.d("PostApiHelper", "모먼트 ID: $momentId")

                    momentId?.let { saveMomentIdToSPF(it) }
                    callback.onSuccess(momentData?.userId, momentId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostApiHelper", "모먼트 생성 실패: ${response.code()} - $errorBody")

                    val errorMessage = response.body()?.error?.reason ?: "알 수 없는 오류 발생"
                    val errorCode = response.body()?.error?.errorCode ?: "UNKNOWN"

                    callback.onFailure("[$errorCode] $errorMessage")
                }
            }

            override fun onFailure(call: Call<MomentResponse>, t: Throwable) {
                Log.e("PostApiHelper", "네트워크 오류 발생: ${t.message}", t)
                callback.onFailure("네트워크 오류 발생: ${t.localizedMessage}")
            }
        })
    }

    private fun saveMomentIdToSPF(momentId: Int) {
        val sharedPreferences = context.getSharedPreferences("MomentData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("MOMENT_ID", momentId)
        editor.apply()
        Log.d("PostApiHelper", "momentId 저장 완료: $momentId")
    }

    // ✅ 추가: momentId를 사용해 moment 상세 데이터 가져오기
    fun fetchUserPageMomentDetail(momentId: Int, callback: UserPageMomentCallback) {
        mypageService.getUserPageMomentDetail(momentId).enqueue(object : Callback<UserPageMomentResponse> {
            override fun onResponse(call: Call<UserPageMomentResponse>, response: Response<UserPageMomentResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let {
                        callback.onSuccess(it)
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "알 수 없는 오류 발생"
                    Log.e("PostApiHelper", "Moment 불러오기 실패: $errorMessage")
                    callback.onFailure("Moment 불러오기 실패: ${response.code()} - ${errorMessage}")
                }
            }

            override fun onFailure(call: Call<UserPageMomentResponse>, t: Throwable) {
                callback.onFailure("네트워크 오류 발생: ${t.localizedMessage}")
            }
        })
    }

    fun deleteMoment(momentId: Int, callback: (Boolean, String?) -> Unit) {
        postService.deleteMoment(momentId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("PostApiHelper", "모먼트 삭제 성공: $momentId")
                    callback(true, null) // 성공 처리
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "삭제 실패"
                    Log.e("PostApiHelper", "모먼트 삭제 실패: ${response.code()} - $errorMessage")
                    callback(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("PostApiHelper", "네트워크 오류 발생: ${t.message}", t)
                callback(false, "네트워크 오류 발생")
            }
        })
    }

}
