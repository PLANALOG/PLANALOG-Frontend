package com.example.planalog.network.SocialLogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.MainActivity
import com.example.planalog.databinding.ActivityWebViewBinding

class KakaologinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    val BASE_URL = "http://15.164.83.14:3000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding 초기화
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WebView 설정
        with(binding.webView) {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url.toString()
                    Log.d("WebViewURL", "Navigated URL: $url")

                    // 리디렉션 URL 감지
                    if (url.startsWith("${BASE_URL}/oauth2/callback/kakao/")) {
                        handleRedirect(Uri.parse(url))
                        return true // WebView에서 처리 중단
                    }
                    return false
                }
            }

            // 네이버 로그인 페이지 로드
            val loginUrl = "${BASE_URL}/oauth2/login/kakao"
            loadUrl(loginUrl)
        }
    }

    private fun handleRedirect(uri: Uri) {
        Log.d("WebViewActivity", "handleRedirect() called with URI: $uri")

        // URL에서 userId 값을 추출하기
        val pathSegments = uri.pathSegments

        // pathSegments의 두 번째 항목이 userId에 해당 (경로가 '/users/{userId}' 형태일 경우)
        if (pathSegments.size >= 2) {
            val userId = pathSegments[1] // 두 번째 세그먼트가 userId
            Log.d("WebViewActivity", "Extracted userId: $userId")

            // userId를 SharedPreferences에 저장
            val spf = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val editor = spf.edit()
            editor.putInt("USER_ID", userId.toInt()) // userId를 Integer로 저장
            editor.apply()

            // MainActivity로 이동
            Log.d("WebViewActivity", "Navigating to MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish()  // WebViewActivity 종료
        } else {
            // userId가 없을 경우 처리
            Log.d("WebViewActivity", "Login failed: No userId found")
            setResult(RESULT_CANCELED)
            finish()  // WebViewActivity 종료
        }
    }
}
