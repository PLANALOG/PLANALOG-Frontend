package com.example.planalog.network

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.RefreshTokenRequest
import com.example.planalog.network.SocialLogin.TokenRefreshResponse
import com.example.planalog.ui.start.LoginActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://15.164.83.14:3000"

        // Authorization 헤더를 자동으로 추가하는 Interceptor
        private class AuthInterceptor(private val context: Context) : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                var accessToken = sharedPreferences.getString("received_access_token", null)

                val requestBuilder = chain.request().newBuilder()
                accessToken?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                val response = chain.proceed(requestBuilder.build())

                // Access 토큰이 만료된 경우 (401)
                if (response.code == 401) {
                    response.close() // 기존 응답 종료

                    val refreshToken = sharedPreferences.getString("received_refresh_token", null)
                    if (!refreshToken.isNullOrEmpty()) {
                        // Refresh 토큰으로 새 Access 토큰 요청
                        refreshAccessToken(context, refreshToken, object : TokenRefreshCallback {
                            override fun onSuccess(newAccessToken: String?) {
                                if (!newAccessToken.isNullOrEmpty()) {
                                    // 새 토큰으로 원래 요청 다시 시도
                                    val newRequest = chain.request().newBuilder()
                                        .addHeader("Authorization", "Bearer $newAccessToken")
                                        .build()
                                    chain.proceed(newRequest)  // 새 토큰으로 요청 진행
                                }
                            }

                            override fun onFailure() {
                                // 토큰 갱신 실패 시 로그아웃 처리
                                handleExpiredTokens(context)
                            }
                        })
                    } else {
                        // Refresh 토큰도 없으면 다시 로그인
                        handleExpiredTokens(context)
                    }
                }
                return response
            }
        }

    // 리프레시토큰 받는 함수
    fun refreshAccessToken(context: Context, refreshToken: String?, callback: TokenRefreshCallback) {

        val apiService = create(LoginService::class.java, context)
        val requestBody = RefreshTokenRequest(refreshToken)

        apiService.refreshToken(requestBody).enqueue(object : Callback<TokenRefreshResponse> {
            override fun onResponse(call: Call<TokenRefreshResponse>, response: retrofit2.Response<TokenRefreshResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        if (responseBody.resultType == "SUCCESS") {
                            val newAccessToken = responseBody.success?.accessToken
                            val newRefreshToken = responseBody.success?.refreshToken

                            val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()

                            // 새 토큰 저장
                            newAccessToken?.let { editor.putString("received_access_token", it) }
                            newRefreshToken?.let { editor.putString("received_refresh_token", it) }
                            editor.apply()

                            Log.d("TokenRefresh", "Access 토큰 재발급 성공: $newAccessToken")
                            callback.onSuccess(newAccessToken)
                        } else {
                            Log.e("TokenRefresh", "오류 발생: ${responseBody.error}")
                            callback.onFailure()
                        }
                    }
                } else {
                    Log.e("TokenRefresh", "응답 실패: ${response.code()}")
                    callback.onFailure()  // 실패 콜백 호출
                }
            }

            override fun onFailure(call: Call<TokenRefreshResponse>, t: Throwable) {
                Log.e("TokenRefresh", "네트워크 오류: ${t.localizedMessage}")
            }
        })
    }

    private fun handleExpiredTokens(context: Context) {
        Toast.makeText(context, "로그인이 만료되었습니다. 다시 로그인하세요.", Toast.LENGTH_SHORT).show()
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply() // 저장된 모든 토큰 삭제

        // 로그인 액티비티로 이동
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    // Retrofit 인스턴스를 생성할 때 Authorization 토큰을 포함하도록 설정
    fun <T> create(service: Class<T>, context: Context): T {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(service)
    }
}