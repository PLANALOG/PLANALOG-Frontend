package com.example.planalog.network.api

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings.Global.putInt
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.planner.PlannerResponse
import com.example.planalog.network.planner.PlannerService
import com.example.planalog.ui.home.calender.SharedViewModel
import com.example.planalog.ui.post.PostFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlannerApiHelper(private val context: Context) {

    private val plannerService = RetrofitClient.create(PlannerService::class.java, context)

    fun getPlanner(userId: String?, date: String?, month: String? , onPlannerRetrieved: (Boolean) -> Unit) {

        plannerService.getPlanners(userId, date, month).enqueue(object : Callback<PlannerResponse> {
            override fun onResponse(
                call: Call<PlannerResponse>,
                response: Response<PlannerResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody == null) {
                        Log.e("PlannerApiHelper", "서버 응답이 null입니다.")
                        Toast.makeText(context, "플래너 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                        onPlannerRetrieved(false) // ❌ 플래너 없음
                        return
                    }

                    if (responseBody.resultType == "SUCCESS") {
                        val plannerSuccess = responseBody.success

                        if (plannerSuccess == null) {
                            Log.e("PlannerApiHelper", "플래너 데이터가 null입니다.")
                            Toast.makeText(context, "플래너가 없습니다.", Toast.LENGTH_SHORT).show()
                            onPlannerRetrieved(false) // ❌ 플래너 없음
                            return
                        }

                        val plannerId = plannerSuccess.plannerId // 🔹 이미 Int이므로 변환 필요 없음

                        if (plannerId <= 0) { // 0 이하 값이 들어오는 경우 예외 처리
                            Log.e("PlannerApiHelper", "플래너 ID가 유효하지 않습니다: $plannerId")
                            Toast.makeText(context, "플래너가 없습니다.", Toast.LENGTH_SHORT).show()
                            onPlannerRetrieved(false) // ❌ 플래너 없음
                            return
                        }

                        // ✅ 플래너 ID를 SharedPreferences에 저장
                        savePlannerIdToSPF(plannerId)

                        Toast.makeText(context, "플래너 데이터 조회 성공", Toast.LENGTH_SHORT).show()
                        Log.d("PlannerApiHelper", "플래너 ID 저장 완료: $plannerId")
                        onPlannerRetrieved(true) // ✅ 플래너 있음
                    } else {
                        Log.e("PlannerApiHelper", "응답 상태가 SUCCESS가 아닙니다: ${responseBody.resultType}")
                        Toast.makeText(context, "플래너가 없습니다.", Toast.LENGTH_SHORT).show()
                        onPlannerRetrieved(false) // ❌ 플래너 없음
                    }

//                    response.body()?.let { responseBody ->
//                        val planners = responseBody.success  // 필요한 데이터만 추출
//                        val plannerId = responseBody.success.plannerId.toInt()
//                        Toast.makeText(context, "플래너 데이터 조회 성공", Toast.LENGTH_SHORT).show()
//                        Log.d("PlannerApiHelper", "플래너 데이터: $planners")
//
//                        savePlannerIdToSPF(plannerId)
//                    }
                } else {
                    Log.e("PlannerApiHelper", "서버 오류: ${response.code()}, ${response.message()}")
                    Toast.makeText(context, "플래너 데이터 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PlannerResponse>, t: Throwable) {
                Log.e("PlannerApiHelper", "네트워크 오류: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun savePlannerIdToSPF(plannerId: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("PlannerSPF", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("PLANNER_ID", plannerId)
        editor.apply()
    }
}