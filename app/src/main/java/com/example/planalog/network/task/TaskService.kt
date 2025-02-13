package com.example.planalog.network.task

import com.example.planalog.network.task.request.AddMultipleTasksRequest
import com.example.planalog.network.task.request.AddTaskRequest
import com.example.planalog.network.task.request.DeleteTasksRequest
import com.example.planalog.network.task.response.AddMultipleTasksResponse
import com.example.planalog.network.task.response.AddTaskResponse
import com.example.planalog.network.task.response.DeleteTasksResponse
import com.example.planalog.network.task.response.GetTasksResponse
import com.example.planalog.network.task.response.TaskStatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface TaskService {
    // 할 일 생성 api
    @POST("/tasks/")
    fun addTask(
        @Body request: AddTaskRequest
    ) : Call<AddTaskResponse>

    // 할 일 삭제
    @HTTP(method = "DELETE", path = "/tasks/", hasBody = true)
    fun deleteTasks(
        @Body request: DeleteTasksRequest
    ) : Call<DeleteTasksResponse>

    // 할 일 조회
    @GET("/tasks/{task_id}")
    fun getTasks(
        @Query("task_id") task_id : Int?,
    ) : Call<GetTasksResponse>

    // 할 일 여러개 생성
    @POST("/tasks/bulk")
    fun addMultipleTasks(
        @Body request: AddMultipleTasksRequest
    ) : Call<AddMultipleTasksResponse>

    @PATCH("/tasks/{task_id}/status")
    fun patchTaskStatus(
        @Query("task_id") task_id : Int,
    ) : Call<TaskStatusResponse>
}