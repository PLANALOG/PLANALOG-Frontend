package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.post.MomentRequest
import com.example.planalog.network.post.MomentRequestContent
import com.example.planalog.network.post.MomentResponse
import com.example.planalog.network.post.PostApiService
import com.example.planalog.network.task.response.AddTaskResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostApiHelper(context : Context) {

    private val postService  = RetrofitClient.create(PostApiService::class.java, context)

    interface UploadPostCallback {
        fun onSuccess(postedId : Int?)
        fun onFailure(errorMessage: String)
    }

    fun uploadPost(title: String, plannerId: Int?, momentContents: List<MomentRequestContent>, callback: UploadPostCallback) {

        val request = MomentRequest(title, plannerId, momentContents)

        postService.createMoment(request).enqueue(object : Callback<MomentResponse> {
            override fun onResponse(call: Call<MomentResponse>, response: Response<MomentResponse>) {
            if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                val momentResponse = response.body()?.success?.data
                val momentId = response.body()?.success?.data?.id
                val postedUserId = response.body()?.success?.data?.userId
                val createdDate = response.body()?.success?.data?.createdAt
                Log.d("PostApiHelper", "모먼트 생성 성공: $momentResponse")
                Log.d("PostApiHelper", "모먼트Id: ${momentId}")
                callback.onSuccess(postedUserId)
            } else {
                Log.e("PostApiHelper", "모먼트 생성 실패: ${response.body()?.error}")
                callback.onFailure("네트워크 오류")
            }
        }

            override fun onFailure(call: Call<MomentResponse>, t: Throwable) {
            Log.e("PostApiHelper", "네트워크 오류 발생: ${t.message}", t)
        }
        })
    }
}