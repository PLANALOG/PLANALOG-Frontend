package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.task.TaskService
import com.example.planalog.network.task.request.AddMultipleTasksRequest
import com.example.planalog.network.task.request.AddTaskRequest
import com.example.planalog.network.task.request.DeleteTasksRequest
import com.example.planalog.network.task.request.TaskCompleteRequest
import com.example.planalog.network.task.response.AddMultipleTasksResponse
import com.example.planalog.network.task.response.AddTaskResponse
import com.example.planalog.network.task.response.DeleteTasksResponse
import com.example.planalog.network.task.response.GetTasksResponse
import com.example.planalog.network.task.response.TaskCompleteResponse
import com.example.planalog.network.task.response.TaskStatusResponse
import com.example.planalog.network.task.response.TodoItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val taskIdsToDelete = mutableListOf<Int>()

class TaskApiHelper(private val context: Context) {
    private val taskService = RetrofitClient.create(TaskService::class.java, context)

    fun addTasks(taskTitle : String,  currentDate : String) {
        val addTaskRequest = AddTaskRequest(taskTitle, currentDate)

        taskService.addTask(addTaskRequest).enqueue(object : Callback<AddTaskResponse> {
            override fun onResponse(call: Call<AddTaskResponse>, response: Response<AddTaskResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val task = response.body()?.success
                    Log.d("TaskApiHelper", "할 일 생성 성공: $task")
                    Log.d("TaskApiHelper", "Task Id: ${task?.id}")
                    Toast.makeText(context, "할 일이 생성되었습니다.", Toast.LENGTH_SHORT).show()

                    task?.id?.let { taskId ->
                        taskIdsToDelete.add(taskId)  // 반환된 할 일 ID를 리스트에 추가
                        Log.d("TaskApiHelper", "할 일 생성 성공 및 ID 저장: $taskId")
                    }

                    getTasks(task?.createdAt.toString())
                } else {
                    Log.e("TaskApiHelper", "할일 생성 실패: 응답 코드=${response.code()}, 메시지=${response.message()}")
                    Toast.makeText(context, "할일 생성 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddTaskResponse>, t: Throwable) {
                Log.e("TaskApiHelper", "네트워크 오류 발생: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun addMultipleTasks(
        titles: List<String>,
        plannerDate: String,
        callback: (Boolean, Int?, List<TodoItem>?, String?) -> Unit
    ) {
        val request = AddMultipleTasksRequest(titles, plannerDate)

        // 로그 추가: API 요청 전에 전달될 데이터 확인
        Log.d("TaskApiHelper", "API 요청: addMultipleTasks - Titles: $titles, Date: $plannerDate")

        taskService.addMultipleTasks(request).enqueue(object : Callback<AddMultipleTasksResponse> {
            override fun onResponse(call: Call<AddMultipleTasksResponse>, response: Response<AddMultipleTasksResponse>) {
                val responseCode = response.code()
                val responseBody = response.body()?.success

                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    Log.d("TaskApiHelper", "성공적인 응답 (코드: $responseCode)")
                    Log.d("TaskApiHelper", "응답 본문: $responseBody")

                    val taskIds = responseBody?.map {it.id} ?: emptyList()
                    Log.d("TaskApiHelper", "추출된 Task ID 목록: $taskIds")

                    callback(true, responseCode, responseBody, null)
                } else {
                    Log.e("TaskApiHelper", "여러개 생성 실패 응답 (코드: $responseCode)")
                    Log.e("TaskApiHelper", "여러개 생성 오류 응답 본문: ${response.body()?.error}")
                    callback(false, responseCode, responseBody, "응답 실패")
                }
            }

            override fun onFailure(call: Call<AddMultipleTasksResponse>, t: Throwable) {
                Log.e("TaskApiHelper", "네트워크 오류 발생: ${t.message}")
                callback(false, null, null, t.message)
            }
        })
    }

    fun getTasks(plannerDate : String) {
        taskService.getTasks(plannerDate).enqueue(object : Callback<GetTasksResponse> {
            override fun onResponse(
                call: Call<GetTasksResponse>,
                response: Response<GetTasksResponse>
            ) {
                if (response.isSuccessful) {
                    val taskResponse = response.body()
                    if (taskResponse != null && taskResponse.resultType == "SUCCESS") {
                        val tasks = taskResponse.tasks
                        tasks.forEach {
                            Log.d("TaskApiHelper","할 일 목록 조회 성공 - 할 일 ID: ${it.id}, 제목: ${it.title}, 완료 여부: ${it.isCompleted}")
                        }
                    } else {
                        Log.d("TaskApiHelper", "응답 성공했지만 데이터 없음")
                        Toast.makeText(context, "오늘 조회된 할 일이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("TaskApiHelper","서버 응답 오류: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetTasksResponse>, t: Throwable) {
                Log.e("TaskApiHelper", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun deleteTasks(taskIds: List<Int?>, onSuccess: (List<Int?>) -> Unit, onFailure: (String) -> Unit) {
        val request = DeleteTasksRequest(taskIds)

        taskService.deleteTasks(request).enqueue(object : Callback<DeleteTasksResponse> {
            override fun onResponse(call: Call<DeleteTasksResponse>, response: Response<DeleteTasksResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val deletedTask = responseBody.success  // 필요한 데이터만 추출
                        val deletedTaskIds = taskIds
                        Toast.makeText(context, "할 일 삭제 성공", Toast.LENGTH_SHORT).show()
                        Log.d("TaskApiHelper", "삭제 결과: $deletedTask, 삭제된 Id: ${deletedTaskIds}")

                        onSuccess(deletedTaskIds)
                    }
                } else {
                    Log.e("TaskApiHelper", "할 일 삭제 실패: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "할 일 삭제 실패", Toast.LENGTH_SHORT).show()
                    onFailure("할 일 삭제 실패")
                }
            }

            override fun onFailure(call: Call<DeleteTasksResponse>, t: Throwable) {
                Log.e("TaskApiHelper", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                onFailure("네트워크 오류")
            }
        })
    }

    fun patchTaskStatus(task_id : Int) {
        taskService.patchTaskStatus(task_id).enqueue(object : Callback<TaskStatusResponse> {
            override fun onResponse(call: Call<TaskStatusResponse>, response: Response<TaskStatusResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val taskStatus = response.body()?.success
                    Toast.makeText(context, "할 일 수정 성공", Toast.LENGTH_SHORT).show()
                    Log.d("TaskApiHelper", "수정 결과: $taskStatus")
                } else {
                    Log.e("TaskApiHelper", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "할 일 수정 실패", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskStatusResponse>, t: Throwable) {
                Log.e("TaskApiHelper", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun toggleTaskComplete(taskIds: List<Int?>, onSuccess: (List<Int?>) -> Unit, onFailure: (String) -> Unit) {
        val request = TaskCompleteRequest(taskIds)
        taskService.toggleTaskComplete(request).enqueue(object : Callback<TaskCompleteResponse> {
            override fun onResponse(call: Call<TaskCompleteResponse>, response: Response<TaskCompleteResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val taskStatus = response.body()?.success
                    Toast.makeText(context, "할 일 완료 여부 수정 성공", Toast.LENGTH_SHORT).show()
                    Log.d("TaskApiHelper", "수정 결과: $taskStatus")
                    onSuccess(taskIds)
                } else {
                    Log.e("TaskApiHelper", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "할 일 완료 여부 수정 실패", Toast.LENGTH_SHORT).show()
                    onFailure("할 일 완료 여부 수정 실패")
                }
            }
            override fun onFailure(call: Call<TaskCompleteResponse>, t: Throwable) {
                Log.e("TaskApiHelper", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                onFailure("네트워크 오류")
            }
        })
    }
}