package com.example.planalog.network.api

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.TokenRequestBody
import com.example.planalog.network.SocialLogin.TokenResponse
import com.example.planalog.ui.start.StartActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginApiHelper(private val context: Context) {
    val loginService = RetrofitClient.create(LoginService::class.java, context)

    fun sendKakaoToken(accessToken : String, refreshToken : String) {
        Log.d("KakaoLogin", "전송할 AccessToken: $accessToken")

        val requestBody = TokenRequestBody(accessToken, refreshToken)
        loginService.sendKakaoAccessToken(requestBody).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(p0: Call<TokenResponse>, response: Response<TokenResponse>) {
                Log.d("KakaoLogin", "서버 응답 수신 - 응답 코드: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("KakaoLogin", "응답 카카오 Access 토큰: ${responseBody?.success?.accessToken.toString()}")
                    Log.d("KakaoLogin", "응답 카카오 Refresh 토큰: ${responseBody?.success?.refreshToken.toString()}")
                    saveReceivedAccessToken(responseBody?.success?.accessToken.toString(), responseBody?.success?.refreshToken.toString())
                } else {
                    Log.e("KakaoLogin", "서버 응답 실패 - 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(p0: Call<TokenResponse>, p1: Throwable) {
                Log.e("KakaoLogin", "네트워크 오류 발생: ${p1.message}", p1)
            }
        })
    }

    fun sendNaverToken(accessToken : String, refreshToken : String) {
        Log.d("NaverLogin", "전송할 AccessToken: $accessToken")

        val requestBody = TokenRequestBody(accessToken, refreshToken)
        loginService.sendNaverAccessToken(requestBody).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(p0: Call<TokenResponse>, response: Response<TokenResponse>) {
                Log.d("NaverLogin", "서버 응답 수신 - 응답 코드: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("NaverLogin", "응답 네이버 Access 토큰: ${responseBody?.success?.accessToken.toString()}")
                    Log.d("NaverLogin", "응답 네이버 Refresh 토큰: ${responseBody?.success?.refreshToken.toString()}")
                    saveReceivedAccessToken(responseBody?.success?.accessToken.toString(), responseBody?.success?.refreshToken.toString())
                } else {
                    Log.e("NaverLogin", "서버 응답 실패 - 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(p0: Call<TokenResponse>, p1: Throwable) {
                Log.e("NaverLogin", "네트워크 오류 발생: ${p1.message}", p1)
            }
        })
    }

    // 서버에서 자체적으로 받아온 토큰 저장 함수
    private fun saveReceivedAccessToken(receivedAccessToken: String?, receivedRefreshToken: String?) {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("received_access_token", receivedAccessToken)
        editor.putString("received_refresh_token", receivedRefreshToken)
        editor.apply()

        Log.d("응답 토큰 저장됨", "access: $receivedAccessToken")
        Log.d("응답 토큰 저장됨", "refresh: $receivedRefreshToken")
    }
}