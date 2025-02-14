package com.example.planalog.network.task_category

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface TaskCategoryService {
    @POST("/task_category/bulk")
    fun addMultipleCtgy(
        @Body request: CtgyAddRequest
    ) : Call<CtgyAddResponse>

    @HTTP(method = "DELETE", path = "/task_category/", hasBody = true)
    fun deleteCtgys(
        @Body request: CtgyDeleteRequest
    ) : Call<CtgyDeleteResponse>
}