package com.example.planalog.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.planalog.MainActivity
import com.example.planalog.R
import com.example.planalog.databinding.ActivityResultBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.user.UserResponse
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.UserUpdateRequest
import com.example.planalog.network.user.UserUpdateResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCombinedTextView()

        val nickname = intent.getStringExtra("nickname") ?: "사용자"
        Log.d("ResultActivity", "받은 닉네임: $nickname")

        val result = intent.getStringExtra("result") ?: "b"

        if (result == "a") {
            binding.iconImageView.setImageResource(R.drawable.ic_memotype)
            binding.userTypeTextView.text = "${nickname}님은\n메모형 사용자입니다."
        } else {
            binding.iconImageView.setImageResource(R.drawable.ic_categorytype)
            binding.userTypeTextView.text = "${nickname}님은\n카테고리형 사용자입니다."
        }


        binding.startButton.setOnClickListener {
            val type = if (result == "a") "memo" else "category"

//            getUserInfo()
//            updateUserInfo(nickname, type)

            // 일단 로그인 여부 상관없이 강제로 메인으로 넘어가게 설정
            val intent = Intent(this@ResultActivity, MainActivity::class.java)
            intent.putExtra("nickname", nickname)
            intent.putExtra("type", type)
            startActivity(intent)
            finish()
        }
    }


    //혹시 몰라서 놔둔 함수 => 지우지 마!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private fun getUserInfo() {

        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val receivedAccessToken = sharedPreferences.getString("received_access_token", null)
        val authorizationHeader = "Bearer $receivedAccessToken"
        val userService = RetrofitClient.create(UserService::class.java, this)

        if (receivedAccessToken.isNullOrEmpty()) {
            Toast.makeText(this, "저장된 액세스 토큰이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        userService.getUserInfo().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val userInfo = response.body()?.success
                    if (userInfo != null) {
                        // JSON 형식으로 UserInfo 로그 출력
                        val userInfoJson = Gson().toJson(userInfo)
                        Toast.makeText(this@ResultActivity, "사용자 정보 확인", Toast.LENGTH_SHORT).show()
                        Log.d("User Info", "UserInfo JSON: $userInfoJson")
                    }

                    // 사용자 정보 확인 후 MainActivity로 이동
                    val intent = Intent(this@ResultActivity, MainActivity::class.java)
                    intent.putExtra("nickname", userInfo?.nickname)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@ResultActivity, "사용자 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                    Log.e("User Info", "실패 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@ResultActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("User Info", "네트워크 오류", t)
            }
        })
    }


    private fun updateUserInfo(nickname: String, type: String) {

        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val receivedAccessToken = sharedPreferences.getString("received_access_token", null)
        val authorizationHeader = "Bearer $receivedAccessToken"
        val userService = RetrofitClient.create(UserService::class.java, this)

        if (receivedAccessToken.isNullOrEmpty()) {
            Toast.makeText(this, "저장된 액세스 토큰이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UserUpdateRequest(nickname = nickname, type = type)

        Log.d("ResultActivity", "서버에 전송할 데이터: $request")

        userService.updateUser(request).enqueue(object : Callback<UserUpdateResponse> {
            override fun onResponse(
                call: Call<UserUpdateResponse>,
                response: Response<UserUpdateResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    Log.d("ResultActivity", "서버 응답 성공: ${response.body()}")
                    Toast.makeText(this@ResultActivity, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ResultActivity", "업데이트 실패: ${response.body()?.error}")
                    Toast.makeText(this@ResultActivity, "업데이트 실패: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserUpdateResponse>, t: Throwable) {
                Log.e("ResultActivity", "네트워크 오류: ${t.localizedMessage}")
                Toast.makeText(this@ResultActivity, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCombinedTextView() {
        val spannable = SpannableStringBuilder()

        val text1 = getString(R.string.app_name)
        val text2 = getString(R.string.on_ready)
        val font1 = ResourcesCompat.getFont(this, R.font.impact)
        val font2 = ResourcesCompat.getFont(this, R.font.pretendard_medium)
        val span1 = TypefaceSpan(font1!!)
        val span2 = TypefaceSpan(font2!!)

        spannable.append(text1)
        spannable.setSpan(span1, 0, text1.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.append(" ")
        spannable.append(text2)
        spannable.setSpan(span2, text1.length + 1, text1.length + 1 + text2.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.titleTextView.text = spannable
    }
}
