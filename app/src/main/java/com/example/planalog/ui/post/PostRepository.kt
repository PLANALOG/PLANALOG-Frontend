package com.example.planalog.ui.post

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostRepository {

    fun postPostData() {
        val postRequest = PostRequest(
            title = "25년 1월 7일",
            status = "draft",
            plannerId = 123,
            postContents = listOf(
                PostContent(1, "오늘 하루 열심히 공부했어요!", "https://image1.com/image1.jpg"),
                PostContent(2, "카페에서 공부 중", "https://image2.com/image2.jpg"),
                PostContent(3, "독서실에서 마지막 정리!", "https://image3.com/image3.jpg")
            )
        )

        val call = RetrofitInstance.api.createPost(postRequest)
        call.enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.resultType == "SUCCESS") {
                        Log.d("API Success", "게시글 생성 성공: ${body.success?.data}")
                    } else {
                        Log.e("API Error", "서버에서 오류 응답: ${body?.error?.reason}")
                    }
                } else {
                    Log.e("API Error", "HTTP 오류 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                Log.e("API Failure", "네트워크 오류 발생: ${t.message}")
            }
        })
    }
}