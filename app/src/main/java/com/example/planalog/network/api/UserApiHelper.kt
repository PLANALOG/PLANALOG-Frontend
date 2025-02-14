package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.request.UserUpdateRequest
import com.example.planalog.network.user.response.NicknameCheckResponse
import com.example.planalog.network.user.response.UserInfo
import com.example.planalog.network.user.response.UserResponse
import com.example.planalog.network.user.response.UserUpdateResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserApiHelper(private val context: Context) {
    private val userService = RetrofitClient.create(UserService::class.java, context)

    fun updateUserInfo(
        context: Context,
        updatedFields: Map<String, Any>, // 수정할 데이터만 포함
        onSuccess: ((Map<String, Any>) -> Unit)? = null, // 성공 시 변경된 값 전달
        onFailure: ((String) -> Unit)? = null
    ) {

        if (updatedFields.isEmpty()) {
            Log.e("UserApiHelper", "업데이트할 데이터가 없습니다.")
            Toast.makeText(context, "업데이트할 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UserUpdateRequest.fromMap(updatedFields)

        userService.updateUser(request).enqueue(object : Callback<UserUpdateResponse> {
            override fun onResponse(
                call: Call<UserUpdateResponse>,
                response: Response<UserUpdateResponse>
            ) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    Log.d("UserApiHelper", "업데이트 성공: ${response.body()}")
                    Toast.makeText(context, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()

                    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    // 요청한 필드만 SharedPreferences에 저장
                    updatedFields.forEach { (key, value) ->
                        when (key) {
                            "nickname" -> editor.putString("nickname", value as String)
                            "type" -> editor.putString("type", value as String)
                            "introduction" -> editor.putString("introduction", value as String)
                            "link" -> editor.putString("link", value as String)
                        }
                    }

                    // 서버 응답에서 user_id가 포함되어 있으면 저장
                    val userId = response.body()?.success?.userId
                    if (userId != null) {
                        editor.putString("user_id", userId)
                        Log.d("UserApiHelper", "저장된 user_id: $userId")
                    }

                    editor.apply()
                    Log.d("UserApiHelper", "저장된 데이터: $updatedFields")

                    onSuccess?.invoke(updatedFields) // 변경된 필드 전달
                } else {
                    val errorMsg = response.body()?.error.toString()
                    Log.e("UserApiHelper", "업데이트 실패: $errorMsg")
                    Toast.makeText(context, "업데이트 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                    onFailure?.invoke(errorMsg)
                }
            }

            override fun onFailure(call: Call<UserUpdateResponse>, t: Throwable) {
                Log.e("UserApiHelper", "네트워크 오류: ${t.localizedMessage}")
                Toast.makeText(context, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                onFailure?.invoke(t.localizedMessage ?: "네트워크 오류 발생")
            }
        })
    }

    fun getUserInfo(
        onSuccess: (UserInfo) -> Unit, // 성공 시 응답 데이터를 전달하는 콜백
        onFailure: ((String) -> Unit)? = null // 실패 시 에러 메시지 전달하는 콜백 (옵션)
    ) {
        userService.getUserInfo().enqueue(object  : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val userInfo = response.body()?.success
                    if (userInfo != null) {
                        // JSON 형식으로 로그 출력
                        val userInfoJson = Gson().toJson(userInfo)
                        Toast.makeText(context, "사용자 정보 확인 성공", Toast.LENGTH_SHORT).show()
                        Log.d("UserApiHelper", "UserInfo JSON: $userInfoJson")

                        // 응답 데이터 콜백으로 전달
                        onSuccess(userInfo)
                    }
                } else {
                    val errorMsg = "사용자 정보 가져오기 실패 (코드: ${response.code()})"
                    Toast.makeText(context, "사용자 정보 가져오기 실패", Toast.LENGTH_SHORT).show()
                    Log.e("UserApiHelper", "실패 코드: ${response.code()}")
                    onFailure?.invoke(errorMsg)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                val errorMsg = "네트워크 오류: ${t.message}"
                Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("UserApiHelper", "네트워크 오류", t)
                onFailure?.invoke(errorMsg)
            }
        })
    }

    fun checkNicknameAvailability(
        context: Context,
        nickname: String,
        onSuccess: (Boolean) -> Unit, // 중복 여부를 전달하는 콜백
        onFailure: ((String) -> Unit)? = null // 실패 시 에러 메시지 전달 (옵션)
    ) {
        userService.idcheck(nickname).enqueue(object : Callback<NicknameCheckResponse> {
            override fun onResponse(
                call: Call<NicknameCheckResponse>,
                response: Response<NicknameCheckResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        Log.d(
                            "UserApiHelper",
                            "서버 응답 성공. 닉네임: $nickname, 응답: ${body.resultType}"
                        )

                        when (body.resultType) {
                            "SUCCESS" -> {
                                val isDuplicated = body.success?.isDuplicated ?: true
                                Log.d("UserApiHelper", "닉네임 중복 여부: $isDuplicated")
                                onSuccess(isDuplicated) // 결과 전달
                            }

                            else -> {
                                val errorMsg = body.error ?: "알 수 없는 오류"
                                Log.e("UserApiHelper", "닉네임 중복 확인 실패: $errorMsg")
                                onFailure?.invoke(errorMsg)
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    val errorMsg = "닉네임 확인 실패 (코드: ${response.code()})"
                    Log.e("UserApiHelper", errorMsg)
                    onFailure?.invoke(errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NicknameCheckResponse>, t: Throwable) {
                val errorMsg = "네트워크 오류: ${t.localizedMessage}"
                Log.e("UserApiHelper", errorMsg)
                onFailure?.invoke(errorMsg)
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        })
    }

}