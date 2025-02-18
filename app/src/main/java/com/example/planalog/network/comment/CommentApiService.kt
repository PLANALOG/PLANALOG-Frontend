package com.example.planalog.network.comment

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentApiService {
    @POST("/moments/{momentId}/comments")
    suspend fun postComment(
        @Path("momentId") momentId: Int,
        @Body commentRequest: CommentRequest
    ): Response<CommentResponse>
}
