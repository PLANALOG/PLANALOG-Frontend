package com.example.planalog.ui.start
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.BuildConfig
import com.example.planalog.databinding.ActivityLoginBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.SocialLogin.KakaologinActivity
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.RefreshTokenRequest
import com.example.planalog.network.SocialLogin.TokenRefreshResponse
import com.example.planalog.network.SocialLogin.TokenRequestBody
import com.example.planalog.network.SocialLogin.TokenResponse
import com.navercorp.nid.NaverIdLoginSDK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

//    private val temporaryRefreshToken = BuildConfig.TEST_REFRESHTOKEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네이버 SDK 초기화
        NaverIdLoginSDK.initialize(this, BuildConfig.NAVER_CLIENT_ID, BuildConfig.NAVER_CLIENT_SECRET, "PLANALOG")


        // ActivityResultLauncher 사용
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    handleNaverLoginSuccess()
                }
                RESULT_CANCELED -> {
                    handleNaverLoginFailure()
                }
            }
        }

        binding.btnNaverLogin.setOnClickListener {

            NaverIdLoginSDK.authenticate(this, launcher)
//            refreshAccessToken(temporaryRefreshToken)
        }

        binding.btnKakaoLogin.setOnClickListener {
            Toast.makeText(this, "Kakao Login Clicked", Toast.LENGTH_SHORT).show()
            // Add your Kakao login logic here
            val intent = Intent(this, KakaologinActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show()
            // Add your Google login logic here
            val intent = Intent(this, StartsetActivity::class.java)
            startActivity(intent)
        }
    }

    // 네이버 로그인 성공 처리
    private fun handleNaverLoginSuccess() {
        val accessToken = NaverIdLoginSDK.getAccessToken()
        val refreshToken = NaverIdLoginSDK.getRefreshToken()

        Log.d("Access 토큰", "Access 네이버 토큰: $accessToken")
        Log.d("Refresh 토큰", "Refresh 네이버 토큰: $refreshToken")

        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
        saveAccessToken(accessToken)
        postAccessTokenToServer(accessToken, refreshToken)
    }

    // 네이버 로그인 실패 처리
    private fun handleNaverLoginFailure() {
        val errorCode = NaverIdLoginSDK.getLastErrorCode().code
        val errorDesc = NaverIdLoginSDK.getLastErrorDescription()
        Toast.makeText(this, "로그인 실패: $errorCode, $errorDesc", Toast.LENGTH_SHORT).show()
    }

    // 토큰 저장 함수
    private fun saveAccessToken(token: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("naver_access_token", token).apply()
        Log.d("저장한 토큰", "저장된 토큰: $token")
    }

    private fun postAccessTokenToServer(accessToken: String?, refreshToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            Toast.makeText(this, "Access Token이 없습니다.", Toast.LENGTH_SHORT).show()
            Log.d("Access 토큰 없음", "$accessToken")
            return
        }

        val tokenService = RetrofitClient.create(LoginService::class.java, this)
        val requestBody = TokenRequestBody(accessToken, refreshToken)
        Log.d("서버에 보낼 토큰", "서버에 보낼 토큰: $accessToken")

        tokenService.sendAccessToken(requestBody).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {

                    val responseBody = response.body()

                    if (responseBody != null && responseBody.resultType == "SUCCESS") {
    //                        Toast.makeText(this@LoginActivity, "토큰 전송 성공", Toast.LENGTH_SHORT).show(

                        val newAccessToken = response.body()?.success?.accessToken
                        val newRefreshToken = response.body()?.success?.refreshToken

                        //토큰 저장
                        newAccessToken?.let { saveAccessToken(it) }
                        newRefreshToken?.let { saveRefreshToken(it) }

//                        saveReceivedAccessToken(newAccessToken)

                        Log.d("전송 토큰", "전송 네이버 토큰: $accessToken")
                        Log.d("응답 토큰", "응답 토큰: $newAccessToken")
                        Log.d("응답 토큰", "응답 토큰: $newRefreshToken")

//                        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//                        val refreshToken = sharedPreferences.getString("naver_refresh_token", null)
//                        refreshAccessToken(refreshToken)

                        moveToNextActivity(newAccessToken)

                    } else {
                        val errorMessage = responseBody?.error ?: "Unknown error"
                        Log.e("토큰 응답 오류", "오류 메시지: $errorMessage")
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "전송 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("토큰 전송 실패", "오류 메시지: ${response}")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "전송 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("네이버 Retrofit", "전송 실패", t)
            }
        })
    }

    // 서버에서 자체적으로 받아온 토큰 저장 함수
//    private fun saveReceivedAccessToken(receivedAccessToken: String?) {
//        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putString("received_access_token", receivedAccessToken)
//        editor.apply()
//    }



    private fun saveRefreshToken(token: String) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("naver_refresh_token", token)
        editor.apply()
        Log.d("TokenRefresh", "저장된 리프레시 토큰: $token")

    }

    // 새 액세스 토큰을 저장하고 다음 액티비티로 이동
    private fun moveToNextActivity(accessToken: String?) {
        val intent = Intent(this, StartsetActivity::class.java)
        accessToken?.let {
            intent.putExtra("access_token", it)
        }
        startActivity(intent)
        finish()  // 현재 액티비티 종료
    }

}