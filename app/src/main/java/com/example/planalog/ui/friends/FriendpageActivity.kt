package com.example.planalog.ui.friends

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.ActivityFriendpageBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.friend.AddFriendService
import com.example.planalog.network.friend.Friend
import com.example.planalog.network.friend.FriendApiService
import com.example.planalog.network.friend.FriendRequest
import com.example.planalog.network.friend.FriendRequestResponse
import com.example.planalog.network.friend.FriendResponse
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.response.FriendProfile
import com.example.planalog.network.user.response.FriendProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendpageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendpageBinding
    private lateinit var userService: UserService
    private lateinit var friendApiService: FriendApiService
    private var userId: String? = null
    private val momentAdapter by lazy { FriendpageMomentAdapter(emptyList()) }
    private var followStatus = false  // 팔로우 상태

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 사용자 ID 가져오기
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        val id = intent.getIntExtra("id", -1)
        if (id != -1) {
            userId = id.toString()
        } else {
            Toast.makeText(this, "유효하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
        }
        Log.d("FriendpageActivity", "userId: $userId, 전달받은 id: $id")

        userService = RetrofitClient.create(UserService::class.java, this)
        friendApiService = RetrofitClient.create(FriendApiService::class.java, this)

        setupRecyclerView()
        userId?.let { fetchFollowingList(it) }
        setupFollowButton()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMoments.apply {
            layoutManager = LinearLayoutManager(this@FriendpageActivity)
            adapter = momentAdapter
        }
    }

    private fun fetchFollowingList(userId: String) {
        friendApiService.getFollowing().enqueue(object : Callback<FriendResponse> {
            override fun onResponse(call: Call<FriendResponse>, response: Response<FriendResponse>) {
                if (response.isSuccessful) {
                    val successData = response.body()?.success
                    val followingList = if (successData is List<*>) {
                        successData.filterIsInstance<Friend>()
                    } else {
                        emptyList()
                    }
                    followStatus = followingList.any { it.id.toString() == userId }
                    toggleFollowButton(followStatus)  // 초기 상태 설정
                } else {
                    Toast.makeText(this@FriendpageActivity, "팔로우 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FriendResponse>, t: Throwable) {
                Log.e("FriendpageActivity", "네트워크 오류: ${t.localizedMessage}", t)
                Toast.makeText(this@FriendpageActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchFriendProfile(userId: String) {
        userService.getFriendProfile(userId)
            .enqueue(object : Callback<FriendProfileResponse> {
                override fun onResponse(call: Call<FriendProfileResponse>, response: Response<FriendProfileResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.success?.let { updateProfileUI(it) }
                    } else {
                        Toast.makeText(this@FriendpageActivity, "프로필 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<FriendProfileResponse>, t: Throwable) {
                    Log.e("FriendpageActivity", "네트워크 오류: ${t.localizedMessage}", t)
                    Toast.makeText(this@FriendpageActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateProfileUI(friendProfile: FriendProfile) {
        binding.userName.text = friendProfile.nickname
        binding.introText.text = friendProfile.introduction

        // 팔로우 상태 설정
        toggleFollowButton(followStatus)
    }

    private fun setupFollowButton() {
        binding.followBtn.setOnClickListener {
            toggleFollowButton(true)
            Toast.makeText(this, "응원하기 클릭", Toast.LENGTH_SHORT).show()
            sendFriendRequest(userId?.toIntOrNull() ?: -1)
        }

        binding.unfollowBtn.setOnClickListener {
            toggleFollowButton(false)
            Toast.makeText(this, "응원중 해제", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFollowButton(isFollowing: Boolean) {
        if (isFollowing) {
            binding.followBtn.visibility = View.GONE
            binding.unfollowBtn.visibility = View.VISIBLE
        } else {
            binding.followBtn.visibility = View.VISIBLE
            binding.unfollowBtn.visibility = View.GONE
        }
    }

    private fun sendFriendRequest(toUserId: Int) {
        if (toUserId == -1) {
            Toast.makeText(this, "유효하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val request = FriendRequest(toUserId)
        val addFriendService = RetrofitClient.create(AddFriendService::class.java, this)
        addFriendService.sendFriendRequest(request).enqueue(object : Callback<FriendRequestResponse> {
            override fun onResponse(call: Call<FriendRequestResponse>, response: Response<FriendRequestResponse>) {
                if (response.isSuccessful) {
                    val successMessage = response.body()?.success?.message ?: "친구 요청이 성공적으로 전송되었습니다."
                    Toast.makeText(this@FriendpageActivity, successMessage, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FriendpageActivity, "친구 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FriendRequestResponse>, t: Throwable) {
                Log.e("FriendpageActivity", "네트워크 오류: ${t.localizedMessage}", t)
                Toast.makeText(this@FriendpageActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
