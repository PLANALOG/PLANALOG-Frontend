package com.example.planalog.network

import java.net.CookieManager
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.planalog.network.startset.Idcheck
import com.example.planalog.network.user.UserService
import java.net.CookiePolicy

object RetrofitClient {

    private const val BASE_URL = "http://15.164.83.14:3000"

    // 로깅 인터셉터 추가 (네트워크 요청/응답 로그 확인용)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // 요청 및 응답 본문 로깅
    }

    // 쿠키 관리 설정 추가
    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)  // 모든 쿠키 허용
    }

    // OkHttp 클라이언트 생성 (쿠키 관리 추가)
    private val httpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))  // 자동 쿠키 관리 추가
        .addInterceptor(loggingInterceptor)
        .build()

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)  // OkHttp 클라이언트 적용
            .build()
    }
}

object ApiService {
    val idcheckService: Idcheck by lazy {
        RetrofitClient.getRetrofit().create(Idcheck::class.java)
    }

    val userService: UserService by lazy {
        RetrofitClient.getRetrofit().create(UserService::class.java)
    }
}
