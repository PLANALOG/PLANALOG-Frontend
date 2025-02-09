package com.example.planalog.ui.friends

import android.content.Context
import android.content.SharedPreferences
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendpageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendpageBinding
    private lateinit var userService: UserService
    private lateinit var friendApiService: FriendApiService
    private var userId: Int? = null
    private val momentAdapter by lazy { FriendpageMomentAdapter(emptyList()) }
    private var followStatus = false  // 팔로우 상태
    private lateinit var spf : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences에서 저장된 결과 가져오기
        spf = getSharedPreferences("search_data", MODE_PRIVATE)
        val savedResults = spf.getStringSet("saved_results", emptySet()) ?: emptySet()
        Log.d("FriendpageActivity", "userData: ${savedResults}")

        // 전달된 JSON 데이터 가져오기
        val userJson = intent.getStringExtra("friendData")

        if (savedResults.isNotEmpty()) {

            // JSON 문자열을 Map<String, Any>로 변환
            val userData: Map<String, Any> = Gson().fromJson(userJson, object : TypeToken<Map<String, Any>>() {}.type)

            // 필요한 정보 추출 예제
            userId = (userData["id"] as? Double)?.toInt()  // id가 Double일 경우 Int로 변환
            val name = userData["name"] as? String ?: "이름 없음"
            val email = userData["email"] as? String ?: "이메일 없음"
            val introduction = userData["introduction"] as? String ?: "소개 없음"
            val link = userData["link"] as? String ?: "링크 없음"
            val nickname = userData["nickname"] as? String ?: "닉네임 없음"

            // UI에 표시하거나 로그로 확인
            Log.d("Friendpage", "userId: ${userId}, Name: $name, Email: $email, Nickname: $nickname")

            binding.userName.text = nickname
        } else {
            Log.e("Friendpage", "전달된 사용자 데이터가 없습니다.")
        }



        userService = RetrofitClient.create(UserService::class.java, this)
        friendApiService = RetrofitClient.create(FriendApiService::class.java, this)

        setupRecyclerView()
        userId.toString()?.let { fetchFollowingList(it) }
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
                    Log.d("친구 팔로우 목록", "follow ${followingList}")
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
            sendFriendRequest(userId.toString() )
        }

        binding.unfollowBtn.setOnClickListener {
            toggleFollowButton(false)
            saveFollowStatus(userId.toString(), false)
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

    private fun sendFriendRequest(toUserId: String) {
        if (toUserId.isNullOrEmpty()) {
            Toast.makeText(this, "유효하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
            Log.e("친구 요청", "유효하지 않은 사용자 ID: $toUserId")
            return
        }
        Log.d("친구 요청", "요청할 사용자 ID: $toUserId")

        val request = FriendRequest(toUserId)
        val addFriendService = RetrofitClient.create(AddFriendService::class.java, this)
        addFriendService.sendFriendRequest(request).enqueue(object : Callback<FriendRequestResponse> {
            override fun onResponse(call: Call<FriendRequestResponse>, response: Response<FriendRequestResponse>) {
                if (response.isSuccessful) {
                    val successMessage = response.body()?.success?.message ?: "친구 요청이 성공적으로 전송되었습니다."
                    Log.d("친구 요청", "성공적으로 친구 요청이 전송됨: $successMessage")
                    Toast.makeText(this@FriendpageActivity, successMessage, Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("친구 요청", "서버 응답 실패: 코드 ${response.code()}, 메시지 ${response.message()}, 응답: $errorBody")


                    if (errorBody?.contains("이미 친구 관계가 존재합니다.") == true) {
                        Log.d("친구 요청", "이미 친구 관계가 존재합니다.")
                        saveFollowStatus(toUserId, true)  // 이미 친구 관계인 경우 상태 저장
                        toggleFollowButton(true)  // UI 업데이트
                    } else {
                        Toast.makeText(this@FriendpageActivity, "친구 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<FriendRequestResponse>, t: Throwable) {
                Log.e("친구 요청", "네트워크 오류 발생: ${t.localizedMessage}", t)
                Toast.makeText(this@FriendpageActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveFollowStatus(userId: String?, isFollowing: Boolean) {
        if (userId == null) return
        with(spf.edit()) {
            putBoolean("follow_status_$userId", isFollowing)
            apply()
        }
        Log.d("팔로우 상태 저장", "사용자 ID: $userId, 상태: $isFollowing")
    }

    private fun getSavedFollowStatus(userId: String?): Boolean {
        if (userId == null) return false
        return spf.getBoolean("follow_status_$userId", false)
    }
}
