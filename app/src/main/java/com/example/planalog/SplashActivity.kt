package com.example.planalog

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivitySplashBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.RetrofitClient.refreshAccessToken
import com.example.planalog.network.TokenRefreshCallback
import com.example.planalog.ui.start.LoginActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000)
    }

    private fun checkLoginStatus() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val type = sharedPreferences.getString("type", null)

        Log.d("SplashActivity", "저장된 user_id: $userId, type: $type")

        if (userId != null) {
            // 유저 정보가 있으면 바로 메인 화면으로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // 유저 정보가 없으면 로그인 화면으로 이동
            Log.d("SplashActivity", "유저 정보가 없습니다. 로그인 화면으로 이동합니다.")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
