package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.task_category.response.TodoItem
import com.example.planalog.network.task_category.request.AddCtgyTaskRequest
import com.example.planalog.network.task_category.response.AddCtgyTaskResponse
import com.example.planalog.network.task_category.request.CtgyAddRequest
import com.example.planalog.network.task_category.response.CtgyAddResponse
import com.example.planalog.network.task_category.request.CtgyDeleteRequest
import com.example.planalog.network.task_category.response.CtgyDeleteResponse
import com.example.planalog.network.task_category.response.CtgyItem
import com.example.planalog.network.task_category.response.GetCtgyResponse
import com.example.planalog.network.task_category.TaskCategoryService
import com.example.planalog.ui.home.ctgy.Category
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskCtgyApiHelper(private val context: Context) {
    private val ctgyService = RetrofitClient.create(TaskCategoryService::class.java, context)

    fun addMultipleCtgy(titles: List<String>, callback: (Boolean, Int?, List<CtgyItem>?, String?) -> Unit) {
        val request = CtgyAddRequest(titles)

        Log.d("TaskApiHelper", "API 요청: addMultipleCtgy - Titles: $titles")

        ctgyService.addMultipleCtgy(request).enqueue(object : Callback<CtgyAddResponse> {
            override fun onResponse(p0: Call<CtgyAddResponse>, response: Response<CtgyAddResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    Log.d("TaskCtgyApiHelper", "성공적인 응답 (코드: ${response.code()})")
                    Log.d("TaskCtgyApiHelper", "응답 본문: ${response.body()?.success}")

                    val ctgyIds = response.body()?.success?.data?.success?.map {it.id}
                    Log.d("TaskCtgyApiHelper", "추출된 카테고리 ID 목록: $ctgyIds")

                    callback(true, response.code(), response.body()?.success?.data?.success, null)
                } else {
                    Log.e("TaskCtgyApiHelper", "여러개 생성 실패 응답 (코드: ${response.code()})")
                    Log.e("TaskCtgyApiHelper", "여러개 생성 오류 응답 본문: ${response.body()?.error}")
                    callback(false, response.code(), null, "응답 실패")
                }
            }

            override fun onFailure(p0: Call<CtgyAddResponse>, p1: Throwable) {
                Log.e("TaskCtgyApiHelper", "네트워크 오류 발생: ${p1.message}")
                callback(false, null, null, p1.message)
            }

        })
    }

    fun addCtgyMultipleTasks(
        taskCategoryId: Int,
        tasks: List<String>,
        plannerDate: String,
        onSuccess: (List<TodoItem>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = AddCtgyTaskRequest(tasks, plannerDate)

        ctgyService.addCtgyTask(taskCategoryId, request)
            .enqueue(object : Callback<AddCtgyTaskResponse> {
                override fun onResponse(call: Call<AddCtgyTaskResponse>, response: Response<AddCtgyTaskResponse>) {
                    if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                        response.body()?.let { responseBody ->
                            val createdTasks = responseBody.success
                            val ctgyTaskIds = createdTasks.map {it.id}
                            Log.d("TaskCtgyApiHelper", "카테고리 할 일이 성공적으로 생성됨: $createdTasks")
                            Log.d("TaskCtgyApiHelper", "추출된 카테고리 하위 할 일 ID 목록: $ctgyTaskIds")
                            onSuccess(createdTasks)
                        }
                    } else {
                        Log.e("TaskCtgyApiHelper", "카테고리 할 일 생성 실패: ${response.code()} - ${response.message()}")
                        onFailure("카테고리 할 일 생성 실패")
                    }
                }

                override fun onFailure(call: Call<AddCtgyTaskResponse>, t: Throwable) {
                    Log.e("TaskCtgyApiHelper", "네트워크 오류 발생: ${t.message}")
                    onFailure("네트워크 오류")
                }
            })
    }

    fun deleteCtgys(
        ctgyIds: List<Int>,
        categoryList: MutableList<Category>, //  HomeFragment에서 전달받도록 변경
        onSuccess: (List<Int>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = CtgyDeleteRequest(ctgyIds)

        ctgyService.deleteCtgys(request).enqueue(object : Callback<CtgyDeleteResponse> {
            override fun onResponse(call: Call<CtgyDeleteResponse>, response: Response<CtgyDeleteResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val deletedCtgyIds = ctgyIds
                        val taskApiHelper = TaskApiHelper(context)
                        Toast.makeText(context, "카테고리 삭제 성공", Toast.LENGTH_SHORT).show()
                        Log.d("TaskCtgyApiHelper", " 삭제된 카테고리 ID: ${deletedCtgyIds}")

                        //  삭제된 카테고리의 하위 할 일 ID 가져오기
                        val taskIdsToDelete = categoryList
                            .filter { it.id in deletedCtgyIds }
                            .flatMap { it.checklists.map { task -> task.taskId } }

                        if (taskIdsToDelete.isNotEmpty()) {
                            //  해당 카테고리의 모든 할 일 삭제
                            taskApiHelper.deleteTasks(taskIdsToDelete,
                                onSuccess = { deletedTaskIds ->
                                    Log.d("TaskCtgyApiHelper", "삭제된 하위 할 일 ID: $deletedTaskIds")
                                },
                                onFailure = { Log.e("TaskCtgyApiHelper", "하위 할 일 삭제 실패") }
                            )
                        }

                        onSuccess(deletedCtgyIds)
                    }
                } else {
                    Log.e("TaskCtgyApiHelper", "카테고리 삭제 실패: ${response.code()} - ${response.message()}")
                    onFailure("카테고리 삭제 실패")
                }
            }

            override fun onFailure(call: Call<CtgyDeleteResponse>, t: Throwable) {
                Log.e("TaskCtgyApiHelper", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                onFailure("네트워크 오류")
            }
        })
    }

    fun getCtgys() {
        ctgyService.getCtgys().enqueue(object : Callback<GetCtgyResponse> {
            override fun onResponse(p0: Call<GetCtgyResponse>, response: Response<GetCtgyResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val getCtgys = responseBody.success
                        Toast.makeText(context, "카테고리 조회 성공", Toast.LENGTH_SHORT).show()
                        Log.d("TaskCtgyApiHelper", " 조회된 카테고리 목록: ${getCtgys}")
                    }
                } else {
                    Log.e("TaskCtgyApiHelper", "카테고리 조회 실패: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(p0: Call<GetCtgyResponse>, p1: Throwable) {
                Log.e("TaskCtgyApiHelper", "네트워크 오류: ${p1.message}", p1)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }

        })
    }
}