package com.example.planalog.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://15.164.83.14:3000"

        // Authorization 헤더를 자동으로 추가하는 Interceptor
        private class AuthInterceptor(private val context: Context) : Interceptor {
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("received_access_token", null)

                val requestBuilder = chain.request().newBuilder()
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                return chain.proceed(requestBuilder.build())
            }
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