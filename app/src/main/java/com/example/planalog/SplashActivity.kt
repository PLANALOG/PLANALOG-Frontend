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
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.RefreshTokenRequest
import com.example.planalog.network.SocialLogin.TokenRefreshResponse
import com.example.planalog.network.TokenRefreshCallback
import com.example.planalog.ui.start.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
//            checkLoginStatus()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun checkLoginStatus() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val type = sharedPreferences.getString("type", null)
        getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val newAccessToken = sharedPreferences.getString("received_access_token", null)
        val newRefreshToken = sharedPreferences.getString("received_refresh_token", null)
        Log.d("SplashActivity", "반환받은 저장된 액세스 토큰: $newAccessToken")
        Log.d("SplashActivity", "반환받은 저장된 리프레시 토큰: $newRefreshToken")

        Log.d("SplashActivity", "저장된 user_id: $userId, type: $type")

//        if (userId != null) {
//            // 유저 정보가 있으면 바로 메인 화면으로 이동
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else {
//            // 유저 정보가 없으면 로그인 화면으로 이동
//            Log.d("SplashActivity", "유저 정보가 없습니다. 로그인 화면으로 이동합니다.")
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

        if (!newAccessToken.isNullOrEmpty()) {
            // 액세스 토큰이 존재하면 바로 메인 액티비티로 이동
            moveToMainActivity()
        } else if (!newRefreshToken.isNullOrEmpty()) {
            // 액세스 토큰이 없고 리프레시 토큰이 존재하면 새 액세스 토큰 요청
            refreshAccessToken(newRefreshToken)
        } else {
            // 토큰이 모두 없으면 로그인 화면으로 이동
            moveToLoginActivity()
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        val tokenService = RetrofitClient.create(LoginService::class.java, this)
        val request = RefreshTokenRequest(refreshToken)

        tokenService.refreshToken(request).enqueue(object : Callback<TokenRefreshResponse> {
            override fun onResponse(call: Call<TokenRefreshResponse>, response: Response<TokenRefreshResponse>) {
                if (response.isSuccessful) {
                    val newAccessToken = response.body()?.success?.accessToken
                    val newRefreshToken = response.body()?.success?.refreshToken

                    if (!newAccessToken.isNullOrEmpty()) {
                        saveNewTokens(newAccessToken, newRefreshToken)
                        moveToMainActivity()
                    } else {
                        Log.e("SplashActivity", "새로운 액세스 토큰을 받지 못했습니다.")
                        moveToLoginActivity()
                    }
                } else {
                    Log.e("SplashActivity", "토큰 갱신 실패: ${response.message()}")
                    moveToLoginActivity()
                }
            }

            override fun onFailure(call: Call<TokenRefreshResponse>, t: Throwable) {
                Log.e("SplashActivity", "토큰 갱신 네트워크 오류: ${t.message}")
                moveToLoginActivity()
            }
        })
    }

    private fun moveToMainActivity() {
        Log.d("SplashActivity", "메인 액티비티로 이동합니다.")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToLoginActivity() {
        Log.d("SplashActivity", "로그인 화면으로 이동합니다.")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveNewTokens(newAccessToken: String, newRefreshToken: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("received_access_token", newAccessToken)
        newRefreshToken?.let {
            editor.putString("received_refresh_token", it)
        }
        editor.apply()
        Log.d("SplashActivity", "새로운 액세스 토큰과 리프레시 토큰 저장 완료")
    }
}
