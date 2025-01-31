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
import com.example.planalog.network.SocialLogin.NaverLoginService
import com.example.planalog.network.SocialLogin.NaverTokenResponse
import com.example.planalog.network.SocialLogin.RefreshTokenRequest
import com.example.planalog.network.SocialLogin.TokenRefreshResponse
import com.example.planalog.network.SocialLogin.TokenRequestBody
import com.example.planalog.utils.isTokenExpired
import com.navercorp.nid.NaverIdLoginSDK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

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
                    val accessToken = NaverIdLoginSDK.getAccessToken()
                    val refreshToken = NaverIdLoginSDK.getRefreshToken()
                    Toast.makeText(this, "로그인 성공: $accessToken", Toast.LENGTH_SHORT).show()
                    Log.d("Access 토큰", "Access 네이버 토큰: $accessToken")
                    Log.d("Refresh 토큰", "Refresh 네이버 토큰: $refreshToken")

                    // SharedPreferences에 네이버에서 받은 토큰 저장
                    saveAccessToken(accessToken)

                    // 서버로 네이버에서 받은 액세스 토큰 전송
                    postAccessTokenToServer(accessToken)

                    // 다음 액티비티로 이동
                    val intent = Intent(this, StartsetActivity::class.java)
                    startActivity(intent)
                }
                RESULT_CANCELED -> {
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDesc = NaverIdLoginSDK.getLastErrorDescription()
                    Toast.makeText(this, "로그인 실패: $errorCode, $errorDesc", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnNaverLogin.setOnClickListener {
            //Toast.makeText(this, "Naver Login Clicked", Toast.LENGTH_SHORT).show()
            // Add your Naver login logic here
            NaverIdLoginSDK.authenticate(this, launcher)
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

    // 네이버 소셜로그인 토큰 저장 함수
    private fun saveAccessToken(token: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("naver_access_token", token)
        editor.apply()  // 비동기적으로 저장
        Log.d("저장한 토큰", "저장된 네이버 토큰: $token")
    }

    private fun postAccessTokenToServer(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) {
//            Toast.makeText(this, "Access Token이 없습니다.", Toast.LENGTH_SHORT).show()
            Log.d("Access 토큰 없음", "$accessToken")
            return
        }

        val TokenService = RetrofitClient.create(NaverLoginService::class.java, this)
        val requestBody = TokenRequestBody(accessToken)
        Log.d("서버에 보낼 토큰", "서버에 보낼 토큰: $accessToken")

        TokenService.sendAccessToken(requestBody).enqueue(object : Callback<NaverTokenResponse> {
            override fun onResponse(call: Call<NaverTokenResponse>, response: Response<NaverTokenResponse>) {
                if (response.isSuccessful) {

                    val responseBody = response.body()

                    if (responseBody != null && responseBody.resultType == "SUCCESS") {
    //                        Toast.makeText(this@LoginActivity, "토큰 전송 성공", Toast.LENGTH_SHORT).show(

                        val newAccessToken = response.body()?.success?.accessToken

                        saveReceivedAccessToken(newAccessToken)

                        Log.d("전송 토큰", "전송 네이버 토큰: $accessToken")
                        Log.d("응답 토큰", "응답 토큰: $newAccessToken")

                        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                        val refreshToken = sharedPreferences.getString("naver_refresh_token", null)
                        refreshAccessToken(refreshToken)

                    } else {
                        val errorMessage = responseBody?.error ?: "Unknown error"
                        Log.e("토큰 응답 오류", "오류 메시지: $errorMessage")
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "전송 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("토큰 전송 실패", "오류 메시지: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NaverTokenResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "전송 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("네이버 Retrofit", "전송 실패", t)
            }
        })
    }

    // 서버에서 자체적으로 받아온 토큰 저장 함수
    private fun saveReceivedAccessToken(receivedAccessToken: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("received_access_token", receivedAccessToken)
        editor.apply()
    }

    // 리프레시토큰 받는 함수
    private fun refreshAccessToken(refreshToken: String?) {
        val apiService = RetrofitClient.create(NaverLoginService::class.java, this)
        val requestBody = RefreshTokenRequest(refreshToken)

        apiService.refreshToken(requestBody).enqueue(object : Callback<TokenRefreshResponse> {
            override fun onResponse(call: Call<TokenRefreshResponse>, response: Response<TokenRefreshResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        if (responseBody.resultType == "SUCCESS") {
                            val newAccessToken = responseBody.success?.accessToken
                            val newRefreshToken = responseBody.success?.refreshToken

                            // Save the new tokens
                            saveReceivedAccessToken(newAccessToken)
                            if (!newRefreshToken.isNullOrEmpty()) {
                                saveRefreshToken(newRefreshToken)
                            }

                            Log.d("TokenRefresh", "Access token 재발급 성공: $newAccessToken")
                            newRefreshToken?.let { Log.d("TokenRefresh", "Refresh token 재발급: $it") }
                        } else {
                            Log.e("TokenRefresh", "오류 발생: ${responseBody.error}")
                        }
                    }
                } else {
                    Log.e("TokenRefresh", "응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TokenRefreshResponse>, t: Throwable) {
                Log.e("TokenRefresh", "네트워크 오류: ${t.localizedMessage}")
            }
        })
    }

    private fun saveRefreshToken(token: String) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("naver_refresh_token", token)
        editor.apply()
    }

}