package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.friend.FriendRequestCallback
import com.example.planalog.network.friend.FriendService
import com.example.planalog.network.friend.request.FriendAddRequest
import com.example.planalog.network.friend.response.FriendAcceptResponse
import com.example.planalog.network.friend.response.FriendAddResponse
import com.example.planalog.network.friend.response.FriendDeleteResponse
import com.example.planalog.network.friend.response.FriendpageMoment
import com.example.planalog.network.friend.response.FriendpageResponse
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.response.FriendProfileResponse
import com.example.planalog.ui.friends.FriendpageActivity

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendApiHelper(private val context: Context) {
    private val friendService = RetrofitClient.create(FriendService::class.java, context)
    private val userService = RetrofitClient.create(UserService::class.java, context)

    fun sendFriendRequest(toUserId: String, callback: FriendRequestCallback) {
        if (toUserId.isNullOrEmpty()) {
            Toast.makeText(context, "유효하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
            Log.e("친구 요청", "유효하지 않은 사용자 ID: $toUserId")
            return
        }
        Log.d("친구 요청", "요청할 사용자 ID: $toUserId")

        val request = FriendAddRequest(toUserId)
        friendService.sendFriendRequest(request).enqueue(object : Callback<FriendAddResponse> {
            override fun onResponse(call: Call<FriendAddResponse>, response: Response<FriendAddResponse>) {
                if (response.isSuccessful) {
                    val successMessage = response.body()?.success?.message
                    Log.d("FriendApiHelper", "친구 요청 성공: ${response.body()?.success?.data}")
                    val friendId = response.body()?.success?.data?.id
                    Log.d("FriendApiHelper", "친구 Id: ${friendId}")
                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                    callback.onRequestSuccess(friendId.toString())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("친구 요청", "서버 응답 실패: 코드 ${response.code()}, 메시지 ${response.message()}, 응답: $errorBody")


                    if (errorBody?.contains("이미 친구 관계가 존재합니다.") == true) {
                        Log.d("친구 요청", "이미 친구 관계가 존재합니다.")
                        callback.onRequestFailure("친구 요청에 실패했습니다.")
                    } else {
                        Toast.makeText(context, "친구 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        callback.onRequestFailure("네트워크 오류가 발생했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<FriendAddResponse>, t: Throwable) {
                Log.e("친구 요청", "네트워크 오류 발생: ${t.localizedMessage}", t)
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun deleteFollowers(followId: String, callback: FriendRequestCallback) {
        if (followId.isEmpty()) {
            Log.e("FriendApiHelper", "유효하지 않은 사용자 ID: $followId")
            return
        }
        Log.d("FriendApiHelper", "친구 삭제할 사용자 ID: $followId")

        friendService.deleteFollowers(followId).enqueue(object : Callback<FriendDeleteResponse> {
            override fun onResponse(call: Call<FriendDeleteResponse>, response: Response<FriendDeleteResponse>) {
                if (response.isSuccessful) {
                    Log.d("FriendApiHelper", "친구 삭제 성공: ${response.body()?.success}")
                    callback.onRequestSuccess(followId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FriendApiHelper", "친구 삭제 실패: 코드 ${response.code()}, 응답: $errorBody")
                    callback.onRequestFailure("친구 삭제에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<FriendDeleteResponse>, t: Throwable) {
                Log.e("FriendApiHelper", "네트워크 오류 발생: ${t.localizedMessage}")
                callback.onRequestFailure("네트워크에 오류가 발생했습니다.")
            }
        })
    }

    fun acceptFriend(friendId : String) {

        friendService.acceptFriend(friendId).enqueue(object : Callback<FriendAcceptResponse> {
            override fun onResponse(call: Call<FriendAcceptResponse>, response: Response<FriendAcceptResponse>) {
                if (response.isSuccessful) {
                    Log.d("FriendApiHelper", "친구 수락 성공: ${response.body()?.success?.data}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FriendApiHelper", "친구 수락 실패: 코드 ${response.code()}, 응답: $errorBody")
                }
            }

            override fun onFailure(call: Call<FriendAcceptResponse>, t: Throwable) {
                Log.e("FriendApiHelper", "네트워크 오류 발생: ${t.localizedMessage}")
            }
        })
    }

    fun getFriendProfile(userId: Int) {
        userService.getFriendProfile(userId.toString()).enqueue(object : Callback<FriendProfileResponse> {
            override fun onResponse(call: Call<FriendProfileResponse>, response: Response<FriendProfileResponse>) {
                if (response.isSuccessful) {
                    val friendProfile = response.body()?.success
                    if (friendProfile != null) {
                        Log.d("FriendApiHelper", "프로필 조회 성공: ${friendProfile.nickname}")

                        // FriendpageActivity의 UI 업데이트
                        (context as? FriendpageActivity)?.runOnUiThread {
                            context.updateFriendProfileUI(friendProfile)
                        }
                    }
                } else {
                    Log.e("FriendApiHelper", "프로필 조회 실패: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<FriendProfileResponse>, t: Throwable) {
                Log.e("FriendApiHelper", "네트워크 오류: ${t.localizedMessage}", t)
            }
        })
    }

    fun fetchFriendMoments(userId: Int, onSuccess: (List<FriendpageMoment>) -> Unit, onFailure: (String) -> Unit) {
        friendService.getFriendpageMoments(userId).enqueue(object : Callback<FriendpageResponse> {
            override fun onResponse(call: Call<FriendpageResponse>, response: Response<FriendpageResponse>) {
                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                    val moments = response.body()?.success?.data ?: emptyList()

                    Log.d("FriendApiHelper", "친구 모먼트 개수: ${moments.size}")

                    // ✅ FriendpageActivity의 UI 업데이트 (binding 사용)
                    (context as? FriendpageActivity)?.updatePostCount(moments.size)

                    onSuccess(moments)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "서버 응답 오류"
                    Log.e("FriendApiHelper", "친구 모먼트 불러오기 실패: $errorMessage")
                    onFailure("친구의 모먼트를 불러올 수 없습니다.")
                }
            }

            override fun onFailure(call: Call<FriendpageResponse>, t: Throwable) {
                Log.e("FriendApiHelper", "네트워크 오류 발생: ${t.message}", t)
                onFailure("네트워크 오류 발생")
            }
        })
    }

}