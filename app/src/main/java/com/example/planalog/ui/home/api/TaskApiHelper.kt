package com.example.planalog.ui.home.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.task.TaskService
import com.example.planalog.network.task.request.AddMultipleTasksRequest
import com.example.planalog.network.task.request.DeleteTasksRequest
import com.example.planalog.network.task.request.addTaskRequest
import com.example.planalog.network.task.response.AddMultipleTasksResponse
import com.example.planalog.network.task.response.AddTaskResponse
import com.example.planalog.network.task.response.DeleteTasksResponse
import com.example.planalog.network.task.response.GetTasksResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val taskIdsToDelete = mutableListOf<Int>()

class TaskApiHelper(private val context: Context) {
    private val taskService = RetrofitClient.create(TaskService::class.java, context)

    fun addTasks(taskTitle : String,  currentDate : String) {
        val addTaskRequest = addTaskRequest(taskTitle, currentDate)

        taskService.addTask(addTaskRequest).enqueue(object : Callback<AddTaskResponse> {
            override fun onResponse(call: Call<AddTaskResponse>, response: Response<AddTaskResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val task = response.body()?.success
                    Log.d("TaskAPI", "할 일 생성 성공: $task")
                    Log.d("Task API", "Task Id: ${task?.id}")
                    Toast.makeText(context, "할 일이 생성되었습니다.", Toast.LENGTH_SHORT).show()

                    task?.id?.let { taskId ->
                        taskIdsToDelete.add(taskId)  // 반환된 할 일 ID를 리스트에 추가
                        Log.d("TaskAPI", "할 일 생성 성공 및 ID 저장: $taskId")
                    }

                    getTasks(task?.id)
                } else {
                    Log.e("TaskAPI", "할일 생성 실패: 응답 코드=${response.code()}, 메시지=${response.message()}")
                    Toast.makeText(context, "할일 생성 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddTaskResponse>, t: Throwable) {
                Log.e("TaskAPI", "네트워크 오류 발생: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun addMultipleTasks(title: List<String>, planner_date: String) {
        val addMultipleTasksRequest = AddMultipleTasksRequest(title, planner_date)

        taskService.addMultipleTasks(addMultipleTasksRequest).enqueue(object : Callback<AddMultipleTasksResponse> {
            override fun onResponse(call: Call<AddMultipleTasksResponse>, response: Response<AddMultipleTasksResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val task = response.body()?.success
                    Log.d("TaskAPI", "할 일 여러개 생성 성공: $task")
                    Toast.makeText(context, "할 일이 여러개 생성되었습니다.", Toast.LENGTH_SHORT).show()

                } else {
                    Log.e("Task API", "할 일 여러개 생성 실패: 응답 코드=${response.code()}, 메시지=${response.message()}")
                    Toast.makeText(context, "할 일 여러개 생성 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddMultipleTasksResponse>, t: Throwable) {
                Log.e("TaskAPI", "네트워크 오류 발생: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getTasks(task_id : Int?) {
        taskService.getTasks(task_id).enqueue(object : Callback<GetTasksResponse> {
            override fun onResponse(
                call: Call<GetTasksResponse>,
                response: Response<GetTasksResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val deletedTask = responseBody.success  // 필요한 데이터만 추출
                        Toast.makeText(context, "할 일 삭제 성공", Toast.LENGTH_SHORT).show()
                        Log.d("Task Delete", "삭제 결과: $deletedTask")
                    }
                } else {
                    Log.e("Task Delete", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "할 일 삭제 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetTasksResponse>, t: Throwable) {
                Log.e("Task Delete", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun deleteTasks(taskIds: List<Int>) {
        val request = DeleteTasksRequest(taskIds)

        taskService.deleteTasks(request).enqueue(object : Callback<DeleteTasksResponse> {
            override fun onResponse(call: Call<DeleteTasksResponse>, response: Response<DeleteTasksResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val deletedTask = responseBody.success  // 필요한 데이터만 추출
                        Toast.makeText(context, "할 일 삭제 성공", Toast.LENGTH_SHORT).show()
                        Log.d("Task Delete", "삭제 결과: $deletedTask")
                    }
                } else {
                    Log.e("Task Delete", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "할 일 삭제 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteTasksResponse>, t: Throwable) {
                Log.e("Task Delete", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }
}