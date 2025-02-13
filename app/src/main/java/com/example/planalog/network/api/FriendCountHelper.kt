package com.example.planalog.network.api

import android.app.Activity
import android.content.Context
import android.util.Log

import com.example.planalog.network.RetrofitClient

import com.example.planalog.network.user.FriendCountResponse
import com.example.planalog.network.user.FriendcountService
import com.example.planalog.ui.friends.FriendpageActivity

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendCountHelper(private val context: Context) {
    private val friendCountService = RetrofitClient.create(FriendcountService::class.java, context)

    fun getUserFriendCount(userId: Int) {
        friendCountService.getUserFriendCount(userId).enqueue(object : Callback<FriendCountResponse> {
            override fun onResponse(call: Call<FriendCountResponse>, response: Response<FriendCountResponse>) {
                if (response.isSuccessful) {
                    val friendCount = response.body()?.success?.data?.friendCount ?: 0
                    Log.d("FriendCountHelper", "친구 수 조회 성공: $friendCount")

                    // FriendpageActivity의 UI 업데이트
                    (context as? Activity)?.runOnUiThread {
                        if (context is FriendpageActivity) {
                            context.updateFriendCountUI(friendCount)
                        }
                    }
                } else {
                    Log.e("FriendCountHelper", "친구 수 조회 실패: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<FriendCountResponse>, t: Throwable) {
                Log.e("FriendCountHelper", "네트워크 오류: ${t.localizedMessage}", t)
            }
        })
    }
}