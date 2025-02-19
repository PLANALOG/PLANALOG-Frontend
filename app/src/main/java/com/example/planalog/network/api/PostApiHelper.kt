package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.post.MomentRequest
import com.example.planalog.network.post.MomentRequestContent
import com.example.planalog.network.post.MomentResponse
import com.example.planalog.network.post.PostApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostApiHelper(private val context: Context) {

    private val postService = RetrofitClient.create(PostApiService::class.java, context)

    interface UploadPostCallback {
        fun onSuccess(postedId: Int?, momentId: Int?)
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
}
