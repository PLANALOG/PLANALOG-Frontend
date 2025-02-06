package com.example.planalog.network.task

import com.example.planalog.network.task.request.AddMultipleTasksRequest
import com.example.planalog.network.task.request.DeleteTasksRequest
import com.example.planalog.network.task.request.addTaskRequest
import com.example.planalog.network.task.response.AddMultipleTasksResponse
import com.example.planalog.network.task.response.AddTaskResponse
import com.example.planalog.network.task.response.DeleteTasksResponse
import com.example.planalog.network.task.response.GetTasksResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TaskService {
    // 할 일 생성 api
    @POST("/tasks/")
    fun addTask(
        @Body request: addTaskRequest
    ) : Call<AddTaskResponse>

    @DELETE("/tasks/")
    fun deleteTasks(
        @Body request: DeleteTasksRequest
    ) : Call<DeleteTasksResponse>

    @GET("/tasks/")
    fun getTasks(
        @Query("task_id") task_id : Int?,
    ) : Call<GetTasksResponse>

    @POST("/tasks/bulk")
    fun addMultipleTasks(
        @Body request: AddMultipleTasksRequest
    ) : Call<AddMultipleTasksResponse>
}