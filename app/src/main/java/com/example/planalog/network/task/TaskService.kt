package com.example.planalog.network.task

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TaskService {
    // 할 일 생성 api
    @POST("/tasks/")
    fun addTask(
        @Body request: addTaskRequest
    ) : Call<AddTaskResponse>
}