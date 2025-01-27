package com.example.planalog.network.SocialLogin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivityWebViewBinding
import com.example.planalog.ui.start.StartsetActivity

class NaverloginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    val BASE_URL = "http://15.164.83.14:3000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.webView) {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()
                    Log.d("WebViewURL", "Navigated URL: $url")

                    if (url.startsWith("${BASE_URL}/oauth2/callback/")) {
                        saveSessionCookies(url)  // 쿠키 저장 함수 호출
                        navigateToNextScreen()
                        return true
                    }
                    return false
                }
            }
            // 네이버 로그인 페이지 로드
            val loginUrl = "${BASE_URL}/oauth2/login/naver"
            loadUrl(loginUrl)
        }
    }

    // 쿠키 저장 메서드
    private fun saveSessionCookies(url: String) {
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(url)
        if (cookies != null) {
            val sharedPreferences = getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("cookies", cookies).apply()
            Log.d("Naverlogin", "세션 쿠키 저장됨: $cookies")

            // 쿠키를 OkHttp 클라이언트에 설정
            setCookiesForHttpClient(cookies)
        } else {
            Log.e("Naverlogin", "쿠키 저장 실패")
        }
    }

    private fun setCookiesForHttpClient(cookies: String) {
        val cookieManager = java.net.CookieManager()
        cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
        val uri = Uri.parse(BASE_URL)
        val cookieList = cookies.split(";")
        cookieList.forEach {
            cookieManager.cookieStore.add(java.net.URI(uri.toString()), java.net.HttpCookie.parse(it)[0])
        }
    }

    // 다음 화면 이동 메서드
    private fun navigateToNextScreen() {
        val intent = Intent(this@NaverloginActivity, StartsetActivity::class.java)
        startActivity(intent)
        finish()
    }
}
