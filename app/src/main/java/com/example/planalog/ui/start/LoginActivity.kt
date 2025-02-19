package com.example.planalog.ui.start
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.planalog.BuildConfig
import com.example.planalog.MainActivity
import com.example.planalog.databinding.ActivityLoginBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.SocialLogin.KakaologinActivity
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.RefreshTokenRequest
import com.example.planalog.network.SocialLogin.TokenRefreshResponse
import com.example.planalog.network.SocialLogin.TokenRequestBody
import com.example.planalog.network.SocialLogin.TokenResponse
import com.example.planalog.network.api.LoginApiHelper
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginApiHelper: LoginApiHelper

//    private val temporaryRefreshToken = BuildConfig.TEST_REFRESHTOKEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네이버 SDK 초기화
        NaverIdLoginSDK.initialize(this, BuildConfig.NAVER_CLIENT_ID, BuildConfig.NAVER_CLIENT_SECRET, "PLANALOG")

        val keyHash = Utility.getKeyHash(this)
        Log.e("해시키", keyHash)

        loginApiHelper = LoginApiHelper(this)

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
        }

        binding.btnKakaoLogin.setOnClickListener {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "카카오톡 로그인 실패", error)
                } else if (token != null) {
                    Log.d("KakaoLogin", "카카오톡 로그인 성공 - AccessToken: ${token.accessToken}")
                    handleKakaoLoginSuccess(token)
                }
            }
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

        Log.d("NaverLogin", "Access 네이버 토큰: $accessToken")
        Log.d("NaverLogin", "Refresh 네이버 토큰: $refreshToken")

        Toast.makeText(this, "네이버 로그인 성공", Toast.LENGTH_SHORT).show()

        saveNaverAccessToken(accessToken, refreshToken)

        loginApiHelper = LoginApiHelper(this)
        loginApiHelper.sendNaverToken(accessToken.toString(), refreshToken.toString())

        moveToNextActivity(accessToken)
    }

    // 네이버 로그인 실패 처리
    private fun handleNaverLoginFailure() {
        val errorCode = NaverIdLoginSDK.getLastErrorCode().code
        val errorDesc = NaverIdLoginSDK.getLastErrorDescription()
        Toast.makeText(this, "로그인 실패: $errorCode, $errorDesc", Toast.LENGTH_SHORT).show()
    }

    // 네이버 토큰 저장 함수
    private fun saveNaverAccessToken(accessToken: String?, refreshToken: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("naver_access_token", accessToken).apply()
        sharedPreferences.edit().putString("naver_refresh_token", refreshToken).apply()
        Log.d("NaverLogin", "저장된 Access 토큰: $accessToken")
        Log.d("NaverLogin", "저장된 Refresh 토큰: $refreshToken")
    }

    // 새 액세스 토큰을 저장하고 다음 액티비티로 이동
    private fun moveToNextActivity(accessToken: String?) {
        val intent = Intent(this, StartActivity::class.java)
        accessToken?.let {
            intent.putExtra("access_token", it)
        }
        startActivity(intent)
        finish()  // 현재 액티비티 종료
    }

    // 로그인 성공 시 토큰 저장 및 사용자 정보 요청
    private fun handleKakaoLoginSuccess(token: OAuthToken) {
        val accessToken = token.accessToken
        val refreshToken = token.refreshToken

        Log.d("KakaoLogin", "Access Token: $accessToken")
        Log.d("KakaoLogin",  "Refresh Token: $refreshToken")

        // 카카오 토큰 저장
        saveKakaoTokens(accessToken, refreshToken)
        sendKakaoTokenToServer()

        // 사용자 정보 요청
        getKakaoUserInfo()
    }

    private fun getKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoLogin", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.d("KakaoLogin", "사용자 정보 -  nickname: ${user.kakaoAccount?.profile?.nickname}, email: ${user.kakaoAccount?.email}")
            }
        }
    }

    private fun saveKakaoTokens(accessToken: String, refreshToken: String?) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("kakao_access_token", accessToken)
            putString("kakao_refresh_token", refreshToken)
            apply()
        }

        // 저장된 값 확인
        val savedAccessToken = sharedPreferences.getString("kakao_access_token", null)
        val savedRefreshToken = sharedPreferences.getString("kakao_refresh_token", null)

        Log.d("KakaoLogin", "토큰 저장 완료 - AccessToken: $savedAccessToken, RefreshToken: $savedRefreshToken")
    }


    private fun getSavedKakaoTokens(): Pair<String, String> {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("kakao_access_token", null)
        val refreshToken = sharedPreferences.getString("kakao_refresh_token", null)

        Log.d("KakaoLogin", "저장된 토큰 확인 - AccessToken: $accessToken, RefreshToken: $refreshToken")

        return Pair(accessToken.orEmpty(), refreshToken.orEmpty())
    }


    private fun sendKakaoTokenToServer() {
        val (accessToken, refreshToken) = getSavedKakaoTokens()

        Log.d("KakaoLogin", "서버로 보낼 토큰 - AccessToken: $accessToken, RefreshToken: $refreshToken")

        loginApiHelper = LoginApiHelper(this)
        loginApiHelper.sendKakaoToken(accessToken, refreshToken)

        moveToNextActivity(accessToken)
    }

}