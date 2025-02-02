package com.example.planalog.ui.profile

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
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
import com.example.planalog.MainActivity
import com.example.planalog.R
import com.example.planalog.databinding.ActivityEditprofileBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.SocialLogin.LoginService
import com.example.planalog.network.SocialLogin.LogoutResponse
import com.example.planalog.network.startset.IdcheckService
import com.example.planalog.network.startset.NicknameCheckResponse
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
    private lateinit var idcheckService: IdcheckService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity started")
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userService = RetrofitClient.create(UserService::class.java, this)
        idcheckService = RetrofitClient.create(IdcheckService::class.java, this)

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
        binding.confirmButton.isEnabled = false


        binding.confirmButton.setOnClickListener {
            val nickname = binding.nameEditText.text.toString()
            val introduction = binding.introEditText.text.toString()
            val link = binding.linkEditText.text.toString()
            updateUserProfile(nickname, introduction, link)
        }

        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nickname = s.toString().trim()
                if (nickname.isNotEmpty()) {
                    checkNicknameAvailability(nickname)
                } else {
                    binding.confirmButton.isEnabled = false
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.logout.setOnClickListener {
            Log.d(TAG, "Logout button clicked")
            logout()
        }
    }

    private fun loadUserProfile() {
        Log.d(TAG, "loadUserProfile: Loading user profile")
        userService.getUserInfo().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "loadUserProfile: Success")
                    response.body()?.success?.let { user ->
                        binding.nameEditText.setText(user.nickname)
                        binding.introEditText.setText(user.introduction)
                        binding.linkEditText.setText(user.link)
                        user.profileImage?.let { imageUrl ->
                            Glide.with(this@EditprofileActivity)
                                .load(imageUrl)
                                .into(binding.profileImage)
                        }
                    }
                } else {
                    Log.e(TAG, "loadUserProfile: Failed - ${response.code()}")
                    Toast.makeText(this@EditprofileActivity, "프로필 정보 로드 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e(TAG, "loadUserProfile: Network error - ${t.message}")
                Toast.makeText(this@EditprofileActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserProfile(nickname: String, introduction: String, link: String) {

        val userService = RetrofitClient.create(UserService::class.java, this)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val type = sharedPreferences.getString("type", "")

        // 요청 객체 생성
        val request = UserUpdateRequest(nickname, type?: "" , introduction, link)

        Log.d(TAG, "서버에 전송할 데이터: $request")

        userService.updateUser(request).enqueue(object : Callback<UserUpdateResponse> {
            override fun onResponse(
                call: Call<UserUpdateResponse>,
                response: Response<UserUpdateResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val resultName = response.body()?.success?.nickname
                    Log.d(TAG, "업데이트 서버 응답 성공: ${response.body()}")
                    Toast.makeText(this@EditprofileActivity, "프로필이 업데이트되었습니다. 바뀐 닉네임: $resultName", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()

                } else {
                    Log.e(TAG, "업데이트 실패: ${response.body()?.error}")
                    Toast.makeText(this@EditprofileActivity, "업데이트 실패: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserUpdateResponse>, t: Throwable) {
                Log.e(TAG, "네트워크 오류: ${t.localizedMessage}")
                Toast.makeText(this@EditprofileActivity, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
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
//                            clearUserPreferences()

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
//    private fun clearUserPreferences() {
//        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        sharedPreferences.edit().clear().apply()  // 모든 데이터 삭제
//        Log.d("EditProfileActivity", "SharedPreferences 초기화 완료")
//    }

    // 로그인 화면으로 이동하는 함수
    private fun navigateToLoginScreen() {
        val intent = Intent(this@EditprofileActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // 이전 스택 비우기
        startActivity(intent)
        finish()  // 현재 액티비티 종료
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


    private fun checkNicknameAvailability(nickname: String) {
        Log.d(TAG, "checkNicknameAvailability: Checking availability for nickname=$nickname")

        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val receivedAccessToken = sharedPreferences.getString("received_access_token", null)
        val idCheckService = RetrofitClient.create(IdcheckService::class.java, this)

        idCheckService.idcheck(nickname).enqueue(object : Callback<NicknameCheckResponse> {
            override fun onResponse(
                call: Call<NicknameCheckResponse>,
                response: Response<NicknameCheckResponse>
            ) {
                Log.d(TAG, "checkNicknameAvailability: Response received")
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        Log.d(
                            TAG,
                            "서버 응답 성공. 닉네임: $nickname, 응답: ${body.resultType}"
                        )

                        when (body.resultType) {
                            "SUCCESS" -> {
                                Log.d(
                                    TAG,
                                    "닉네임 중복 여부: ${body.success?.isDuplicated}"
                                )
                                handleSuccessResponse(body.success?.isDuplicated ?: true)
                            }

                            else -> {
                                Log.e(TAG, "오류 발생: ${body.error ?: "알 수 없는 오류"}")
                                showErrorMessage(body.error ?: "알 수 없는 오류가 발생했습니다.")
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<NicknameCheckResponse>, t: Throwable) {
                Log.e(TAG, "checkNicknameAvailability: Network error - ${t.localizedMessage}")
                showErrorMessage("네트워크 오류: ${t.localizedMessage}")
            }
        })
    }

    private fun handleSuccessResponse(isDuplicated: Boolean) {
        binding.confirmButton.isEnabled = !isDuplicated
        if (isDuplicated) {
            Toast.makeText(this, "닉네임이 중복됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    companion object {
        private const val galleryRequestCode = 100
        private const val TAG = "프로필편집"
    }
}
