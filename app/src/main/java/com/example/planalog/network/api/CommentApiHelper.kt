package com.example.planalog.network.api

import android.content.Context
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.comment.CommentApiService
import com.example.planalog.network.comment.CommentContent
import com.example.planalog.network.comment.CommentRequest
import com.example.planalog.network.comment.CommentResponse
import com.example.planalog.network.comment.SuccessResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class CommentApiHelper(private val context: Context) {

    private val commentApiService: CommentApiService = RetrofitClient.create(CommentApiService::class.java, context)

    suspend fun postComment(momentId: Int, content: String, callback: CommentCallback) {
        try {
            val response: Response<CommentResponse> = withContext(Dispatchers.IO) {
                commentApiService.postComment(momentId, CommentRequest(CommentContent(content)))
            }

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.resultType == "SUCCESS") {
                    callback.onSuccess(body.success!!)
                } else {
                    callback.onFailure(body.error?.reason ?: "알 수 없는 오류 발생")
                }
            } else {
                callback.onFailure("서버 응답 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            callback.onFailure("네트워크 오류: ${e.localizedMessage}")
        }
    }

    interface CommentCallback {
        fun onSuccess(comment: SuccessResponse)
        fun onFailure(errorMessage: String)
    }
}
