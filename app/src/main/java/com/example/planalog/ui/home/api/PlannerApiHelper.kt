package com.example.planalog.ui.home.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.planner.PlannerResponse
import com.example.planalog.network.planner.PlannerService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlannerApiHelper(private val context: Context) {

    private val plannerService = RetrofitClient.create(PlannerService::class.java, context)

    fun getPlanner(userId: String?, date: String?, month: String?) {

        plannerService.getPlanners(userId, date, month).enqueue(object : Callback<PlannerResponse> {
            override fun onResponse(
                call: Call<PlannerResponse>,
                response: Response<PlannerResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    response.body()?.let { responseBody ->
                        val planners = responseBody.success  // 필요한 데이터만 추출
                        Toast.makeText(context, "플래너 데이터 조회 성공", Toast.LENGTH_SHORT).show()
                        Log.d("Planner", "플래너 데이터: $planners")
                    }
                } else {
                    Log.e("Planner", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "플래너 데이터 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PlannerResponse>, t: Throwable) {
                Log.e("Planner", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }
}