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
import com.example.planalog.network.friend.FriendApiService
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
    private var friendId: Int? = null  // 전달받은 friendId 저장
    private var userId: String? = null  // SharedPreferences에서 불러올 유저 ID (문자열로 변환됨)
    private val momentAdapter by lazy { FriendpageMomentAdapter(emptyList()) }
    private var followStatus = false  // 팔로우 상태

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰 바인딩 초기화
        binding = ActivityFriendpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences에서 사용자 ID 가져오기
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        // Intent로 전달된 friendId 가져오기
        friendId = intent.getIntExtra("friendId", -1)
        if (friendId != -1) {
            userId = friendId.toString()
        } else {
            Toast.makeText(this, "유효하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
        }

        Log.d("FriendpageActivity", "userId: $userId, 전달받은 friendId: $friendId")

        // Retrofit 서비스 초기화
        userService = RetrofitClient.create(UserService::class.java, this)
        friendApiService = RetrofitClient.create(FriendApiService::class.java, this)

        // RecyclerView 설정
        setupRecyclerView()

        // 친구 프로필 정보 및 팔로우 목록 가져오기
        userId?.let {
            fetchFollowingList(it)  // 팔로우 상태 확인 후 프로필 정보 가져오기
        }

        // 버튼 UI 설정
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
                    val friendResponse = response.body()
                    val followingList = friendResponse?.success ?: emptyList()

                    // userId와 friendId가 일치하는지 확인
                    followStatus = followingList.any { it.friendId.toString() == userId }

                    // 팔로우 상태에 따라 프로필 정보 가져오기
                    fetchFriendProfile(userId)
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
                        val friendProfile = response.body()?.success
                        if (friendProfile != null) {
                            updateProfileUI(friendProfile)
                        } else {
                            val errorMessage = response.body()?.error?.reason ?: "알 수 없는 오류"
                            Toast.makeText(this@FriendpageActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
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

        // 프로필 이미지 로드
//        friendProfile.profileImage?.let {
//            Glide.with(this).load(it).into(binding.profileImage)
//        }

        // 팔로우 상태에 따른 버튼 설정
        if (followStatus) {
            binding.followBtn.visibility = View.GONE
            binding.unfollowBtn.visibility = View.VISIBLE
        } else {
            binding.followBtn.visibility = View.VISIBLE
            binding.unfollowBtn.visibility = View.GONE
        }
    }

    private fun setupFollowButton() {
        binding.followBtn.setOnClickListener {
            binding.followBtn.visibility = View.GONE
            binding.unfollowBtn.visibility = View.VISIBLE
            Toast.makeText(this, "응원하기 클릭", Toast.LENGTH_SHORT).show()
        }

        binding.unfollowBtn.setOnClickListener {
            binding.unfollowBtn.visibility = View.GONE
            binding.followBtn.visibility = View.VISIBLE
            Toast.makeText(this, "응원중 해제", Toast.LENGTH_SHORT).show()
        }
    }
}
