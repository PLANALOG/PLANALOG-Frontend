package com.example.planalog.network.task_category

import com.example.planalog.network.task_category.request.AddCtgyTaskRequest
import com.example.planalog.network.task_category.request.CtgyAddRequest
import com.example.planalog.network.task_category.request.CtgyDeleteRequest
import com.example.planalog.network.task_category.response.AddCtgyTaskResponse
import com.example.planalog.network.task_category.response.CtgyAddResponse
import com.example.planalog.network.task_category.response.CtgyDeleteResponse
import com.example.planalog.network.task_category.response.GetCtgyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskCategoryService {
    @POST("/task_category/bulk")
    fun addMultipleCtgy(
        @Body request: CtgyAddRequest
    ) : Call<CtgyAddResponse>

    @HTTP(method = "DELETE", path = "/task_category/", hasBody = true)
    fun deleteCtgys(
        @Body request: CtgyDeleteRequest
    ) : Call<CtgyDeleteResponse>

    @POST("/task_category/{task_category_id}/tasks/bulk")
    fun addCtgyTask(
        @Path("task_category_id") taskCategoryId: Int,
        @Body request : AddCtgyTaskRequest
    ) : Call<AddCtgyTaskResponse>

    @GET("/task_category/")
    fun getCtgys() :Call<GetCtgyResponse>
}