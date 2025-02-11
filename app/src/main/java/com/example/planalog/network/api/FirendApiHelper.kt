package com.example.planalog.network.api

import android.content.Context
import android.util.Log
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.friend.FriendApiService
import com.example.planalog.network.friend.response.FriendDeleteResponse
import com.example.planalog.network.friend.FriendRequestCallback
import com.example.planalog.network.friend.request.FriendRequest
import com.example.planalog.network.friend.request.FriendRequestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendApiHelper(private val context: Context) {
    private val friendService = RetrofitClient.create(FriendApiService::class.java, context)

    fun sendFriendRequest(toUserId: String, callback: FriendRequestCallback) {
        if (toUserId.isEmpty()) {
            callback.onRequestFailure("유효하지 않은 사용자입니다.")
            Log.e("FriendApiHelper", "유효하지 않은 사용자 ID: $toUserId")
            return
        }
        Log.d("FriendApiHelper", "친구 요청할 사용자 ID: $toUserId")

        val request = FriendRequest(toUserId)
        friendService.sendFriendRequest(request).enqueue(object : Callback<FriendRequestResponse> {
            override fun onResponse(call: Call<FriendRequestResponse>, response: Response<FriendRequestResponse>) {
                if (response.isSuccessful) {
                    Log.d("FriendApiHelper", "친구 요청 성공: ${response.body()?.success?.message}")
                    val friendId = response.body()?.success?.data?.id
                    Log.d("FriendApiHelper", "친구 Id: ${friendId}")
                    callback.onRequestSuccess(friendId.toString())  // 성공 콜백 호출
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FriendApiHelper", "친구 요청 실패: 코드 ${response.code()}, 응답: $errorBody")
                    callback.onRequestFailure("친구 요청에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<FriendRequestResponse>, t: Throwable) {
                Log.e("FriendApiHelper", "네트워크 오류 발생: ${t.localizedMessage}")
                callback.onRequestFailure("네트워크 오류가 발생했습니다.")
            }
        })
    }

    fun deleteFollowers(friendId: String) {
        if (friendId.isEmpty()) {
            Log.e("FriendApiHelper", "유효하지 않은 사용자 ID: $friendId")
            return
        }
        Log.d("FriendApiHelper", "친구 삭제할 사용자 ID: $friendId")

        friendService.deleteFollowers(friendId).enqueue(object : Callback<FriendDeleteResponse> {
            override fun onResponse(call: Call<FriendDeleteResponse>, response: Response<FriendDeleteResponse>) {
                if (response.isSuccessful) {
                    Log.d("FriendApiHelper", "친구 삭제 성공: ${response.body()?.success}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FriendApiHelper", "친구 삭제 실패: 코드 ${response.code()}, 응답: $errorBody")
                }
            }

            override fun onFailure(call: Call<FriendDeleteResponse>, t: Throwable) {
                Log.e("FriendApiHelper", "네트워크 오류 발생: ${t.localizedMessage}")
            }
        })
    }
}