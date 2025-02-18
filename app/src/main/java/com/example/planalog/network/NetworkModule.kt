package com.example.planalog.network

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
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

    private const val BASE_URL = "https://planalog.site"

    // Authorization 헤더를 추가하는 Interceptor
    private class AuthInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val accessToken = sharedPreferences.getString("received_access_token", null)

            val requestBuilder = chain.request().newBuilder()
            accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            return chain.proceed(requestBuilder.build())
        }
    }

    // Retrofit 인스턴스 생성 함수
    fun <T> create(service: Class<T>, context: Context): T {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(service)
    }
}
