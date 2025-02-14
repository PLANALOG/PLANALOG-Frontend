package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.task.request.DeleteTasksRequest
import com.example.planalog.network.task.response.DeleteTasksResponse
import com.example.planalog.network.task.response.TodoItem
import com.example.planalog.network.task_category.CtgyAddRequest
import com.example.planalog.network.task_category.CtgyAddResponse
import com.example.planalog.network.task_category.CtgyDeleteRequest
import com.example.planalog.network.task_category.CtgyDeleteResponse
import com.example.planalog.network.task_category.CtgyItem
import com.example.planalog.network.task_category.TaskCategoryService
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
                    Log.d("TaskCtgyApiHelper", "추출된 Task ID 목록: $ctgyIds")

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

    fun deleteCtgys(ctgyIds: List<Int>, onSuccess: (List<Int>) -> Unit, onFailure: (String) -> Unit) {
        val request = CtgyDeleteRequest(ctgyIds)

        ctgyService.deleteCtgys(request).enqueue(object : Callback<CtgyDeleteResponse> {
            override fun onResponse(call: Call<CtgyDeleteResponse>, response: Response<CtgyDeleteResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val deletedCtgy = responseBody.success
                        val deletedCtgyIds = ctgyIds
                        Toast.makeText(context, "카테고리 삭제 성공", Toast.LENGTH_SHORT).show()
                        Log.d("TaskCtgyApiHelper", "삭제 결과: $deletedCtgy, 삭제된 Id: ${deletedCtgyIds}")

                        onSuccess(deletedCtgyIds)
                    }
                } else {
                    Log.e("TaskCtgyApiHelper", "카테고리 삭제 실패: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "카테고리 삭제 실패", Toast.LENGTH_SHORT).show()
                    onFailure("할 일 삭제 실패")
                }
            }

            override fun onFailure(call: Call<CtgyDeleteResponse>, t: Throwable) {
                Log.e("TaskCtgyApiHelper", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                onFailure("네트워크 오류")
            }
        })
    }
}