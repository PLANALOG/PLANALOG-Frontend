package com.example.planalog.ui.profile

import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivityWebViewBinding

class WebviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("URL") ?: "https://default-url.com"
        Log.d("WebviewActivity", "WebView에서 로드할 URL: $url") // 디버깅 로그 추가

        // 웹뷰 설정
        binding.webView.apply {

            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(url)
        }

        binding.webView.settings.apply {
            javaScriptEnabled = true // JavaScript 활성화
            domStorageEnabled = true // DOM Storage (localStorage & sessionStorage) 활성화
            cacheMode = WebSettings.LOAD_DEFAULT // 캐시 설정
            allowFileAccess = true // 파일 접근 허용
            allowContentAccess = true // 컨텐츠 접근 허용
            loadWithOverviewMode = true
            useWideViewPort = true
        }

    }

}