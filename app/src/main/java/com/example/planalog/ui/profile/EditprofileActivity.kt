package com.example.planalog.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.ActivityEditprofileBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.LogoutResponse
import com.example.planalog.network.api.UserApiHelper
import com.example.planalog.network.user.response.NicknameCheckResponse
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.request.UserUpdateRequest
import com.example.planalog.network.user.response.UserProfileImgResponse
import com.example.planalog.network.user.response.UserResponse
import com.example.planalog.network.user.response.UserUpdateResponse
import com.example.planalog.ui.start.LoginActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditprofileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditprofileBinding
    private lateinit var userService: UserService
    private lateinit var userApiHelper : UserApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity started")
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userService = RetrofitClient.create(UserService::class.java, this)
        userApiHelper = UserApiHelper(this)

        loadUserProfile()

        // 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            finish()
        }

        binding.exit.setOnClickListener {
            Log.d(TAG, "Exit button clicked")
            startActivity(Intent(this, ExitpopupActivity::class.java))
        }

        binding.icAddprofile.setOnClickListener {
            Log.d(TAG, "Add profile image button clicked")
            if (hasGalleryPermission()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }
//        binding.confirmButton.isEnabled = false


        binding.confirmButton.setOnClickListener {
            val nickname = binding.nameEditText.text.toString()
            val introduction = binding.introEditText.text.toString()
            val link = binding.linkEditText.text.toString()

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val currentNickname = sharedPreferences.getString("nickname", "")

            // ✅ 닉네임이 기존 값과 동일하면 업데이트하지 않음
            if (nickname == currentNickname) {
                Toast.makeText(this, "닉네임을 변경해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkNicknameAvailability(nickname, introduction, link)
        }

        binding.logout.setOnClickListener {
            Log.d(TAG, "Logout button clicked")
            logout()
        }

        binding.terms.setOnClickListener {
            val url = "https://well-sheet-c6d.notion.site/19407a632e98800493d0d4e7d936e3da?pvs=4"
            Log.d("EditprofileActivity", "이용약관 클릭됨 - 전달할 URL: $url")
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra("URL", url)
            startActivity(intent)
        }

        binding.privacyPolicy.setOnClickListener {
            val url = "http://well-sheet-c6d.notion.site"
            Log.d("EditprofileActivity", "개인정보 처리방침 클릭됨 - 전달할 URL: $url")
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra("URL", url)
            startActivity(intent)
        }
    }

    private fun loadUserProfile() {
        userApiHelper = UserApiHelper(this)

        userApiHelper.getUserInfo(
            onSuccess = { userInfo ->
                val nickname = userInfo.nickname
                val type = userInfo.type
                Log.d("EditProfileActivity", "닉네임: $nickname, 타입: $type")

                binding.nameEditText.setText(userInfo.nickname)
                binding.introEditText.setText(userInfo.introduction)
                binding.linkEditText.setText(userInfo.link)
                userInfo.profileImage?.let { imageUrl ->
                    Glide.with(this@EditprofileActivity)
                        .load(imageUrl)
                        .into(binding.profileImage)
                }
            },
            onFailure = { errorMsg ->
                Log.e("EditProfileActivity", "loadUserProfile: Failed - ${errorMsg}")
                Toast.makeText(this@EditprofileActivity, "프로필 정보 로드 실패", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun hasGalleryPermission(): Boolean {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        val hasPermission = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "hasGalleryPermission: $hasPermission")
        return hasPermission
    }

    private fun requestGalleryPermission() {
        Log.d(TAG, "requestGalleryPermission: Requesting gallery permission")
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        ActivityCompat.requestPermissions(this, arrayOf(permission), galleryRequestCode)
    }

    private fun openGallery() {
        Log.d(TAG, "openGallery: Opening gallery")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            Log.d(TAG, "galleryLauncher: Image selected")
            result.data?.data?.let { updateProfileImage(it) }
        } else {
            Log.e(TAG, "galleryLauncher: No image selected or result canceled")
        }
    }

    private fun updateProfileImage(imageUri: Uri) {
        Log.d(TAG, "updateProfileImage: Updating profile image - Uri: $imageUri")
        val imageFile = getFileFromUri(imageUri)
        if (imageFile != null) {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            val basicImageBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "")

            userService.uploadProfileImage(imagePart, basicImageBody).enqueue(object : Callback<UserProfileImgResponse> {
                override fun onResponse(call: Call<UserProfileImgResponse>, response: Response<UserProfileImgResponse>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "updateProfileImage: Upload success")
                        response.body()?.success?.savedUrl?.let { savedUrl ->
                            Glide.with(this@EditprofileActivity).load(savedUrl)
                                .placeholder(R.drawable.ic_myprofile).into(binding.profileImage)
                        }
                        Toast.makeText(this@EditprofileActivity, "프로필 이미지가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "updateProfileImage: Upload failed - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<UserProfileImgResponse>, t: Throwable) {
                    Log.e(TAG, "updateProfileImage: Network error - ${t.message}")
                }
            })
        } else {
            Log.e(TAG, "updateProfileImage: Failed to get file from Uri")
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            Log.d(TAG, "getFileFromUri: Converting Uri to File - Uri: $uri")
            val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            Log.d(TAG, "getFileFromUri: Temporary file created at ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "getFileFromUri: Error converting Uri to File - ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == galleryRequestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: Gallery permission granted")
            openGallery()
        } else {
            Log.e(TAG, "onRequestPermissionsResult: Gallery permission denied")
            Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkNicknameAvailability(nickname: String, introduction: String, link: String) {

        Log.d(TAG, "닉네임 중복 확인 중: $nickname")

        userApiHelper.checkNicknameAvailability(
            context = this,
            nickname = nickname,
            onSuccess = { isDuplicated ->
                if (isDuplicated) {
                    Toast.makeText(this, "닉네임을 변경해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    updateUserProfile(nickname, introduction, link)
                }
            },
            onFailure = { errorMsg ->
                Log.e("EditProfileActivity", "닉네임 확인 실패: $errorMsg")
            }
        )
    }

    private fun updateUserProfile(nickname: String, introduction: String, link: String) {
        val userApiHelper = UserApiHelper(this)

        val updatedFields = mutableMapOf<String, Any>(
            "nickname" to nickname // ✅ 닉네임 필드는 항상 포함
        )

        if (introduction.isNotEmpty()) updatedFields["introduction"] = introduction
        if (link.isNotEmpty()) updatedFields["link"] = link

        userApiHelper.updateUserInfo(
            context = this,
            updatedFields = updatedFields,
            onSuccess = {
                Log.d(TAG, "프로필 업데이트 성공")
                Toast.makeText(this, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            },
            onFailure = { errorMsg ->
                Log.e(TAG, "프로필 업데이트 실패: $errorMsg")
                Toast.makeText(this, "업데이트 실패: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
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

                            // SharedPreferences 초기화
                            clearUserPreferences()

                            // 로그인 화면으로 이동
                            navigateToLoginScreen()
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

    // SharedPreferences 초기화
    private fun clearUserPreferences() {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()  // 모든 데이터 삭제
        // 초기화 후 SharedPreferences에 데이터가 남아 있는지 확인
        val accessToken = sharedPreferences.getString("received_access_token", "토큰 없음")
        val refreshToken = sharedPreferences.getString("received_refresh_token", "토큰 없음")
        Log.d("EditProfileActivity", "SharedPreferences 초기화 완료")
        Log.d("EditProfileActivity", "초기화 후 Access Token: $accessToken")
        Log.d("EditProfileActivity", "초기화 후 Refresh Token: $refreshToken")
    }

    // 로그인 화면으로 이동하는 함수
    private fun navigateToLoginScreen() {
        val intent = Intent(this@EditprofileActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // 이전 스택 비우기
        startActivity(intent)
        finish()  // 현재 액티비티 종료
    }
    companion object {
        private const val galleryRequestCode = 100
        private const val TAG = "프로필편집"
    }
}
