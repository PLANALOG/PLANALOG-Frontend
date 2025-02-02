package com.example.planalog.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.databinding.ActivityEditprofileBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.LogoutResponse
import com.example.planalog.network.SocialLogin.TokenRefreshResponse
import com.example.planalog.network.user.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class EditprofileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditprofileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding 초기화
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.exit.setOnClickListener {
            val intent = Intent(this, ExitpopupActivity::class.java)
            startActivity(intent)
        }

        binding.icAddprofile.setOnClickListener{
            val intent = Intent(this, EditcharacterActivity::class.java)
            startActivity(intent)
        }

        binding.confirmButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val intro = binding.introEditText.text.toString()
            val link = binding.linkEditText.text.toString()

            // 저장 로직 추가
            Toast.makeText(
                this@EditprofileActivity,
                "저장됨: 이름=$name, 소개=$intro, 링크=$link",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.logout.setOnClickListener {
            logout()
        }
    }


    private fun logout() {
        val logoutService = RetrofitClient.create(LoginService::class.java, this)

        logoutService.logout().enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        if (responseBody.resultType == "SUCCESS") {
                            val responseBody = responseBody.success
                            Toast.makeText(this@EditprofileActivity, "로그아웃 성공: ${response.body()}", Toast.LENGTH_SHORT).show()
                            Log.d("EditProfileActivity", "로그아웃 성공: $responseBody")

                        } else {
                            Log.e("EditProfileActivity", "오류 발생: ${responseBody.error}")
                        }
                    }
                } else {
                    Log.e("EditProfileActivity", "응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                Log.e("EditProfileActivity", "네트워크 오류: ${t.localizedMessage}")
            }
        })
    }
}
