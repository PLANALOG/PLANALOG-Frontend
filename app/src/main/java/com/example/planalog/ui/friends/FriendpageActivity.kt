package com.example.planalog.ui.friends

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.ActivityFriendpageBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.api.FriendApiHelper
import com.example.planalog.network.friend.FriendApiService
import com.example.planalog.network.friend.FriendRequestCallback
import com.example.planalog.network.friend.response.Friend
import com.example.planalog.network.friend.response.FriendResponse
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
    private var friendUserId: Int? = null
    private var friendFollowId : String? = null
    private val momentAdapter by lazy { FriendpageMomentAdapter(emptyList()) }
    private var followStatus = false  // 팔로우 상태
    private lateinit var spf : SharedPreferences
    private lateinit var friendApiHelper : FriendApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendApiHelper = FriendApiHelper(this)
        spf = getSharedPreferences("follow_status", Context.MODE_PRIVATE)

        // 전달된 JSON 데이터 가져오기
        val userJson = intent.getStringExtra("friendData")

        // JSON 문자열을 Map<String, Any>로 변환
        val userData: Map<String, Any> = Gson().fromJson(userJson, object : TypeToken<Map<String, Any>>() {}.type)

            // 필요한 정보 추출 예제
        friendUserId = (userData["id"] as? Double)?.toInt()  // id가 Double일 경우 Int로 변환
        val name = userData["name"] as? String ?: "이름 없음"
        val email = userData["email"] as? String ?: "이메일 없음"
        val nickname = userData["nickname"] as? String ?: "닉네임 없음"

            // UI에 표시하거나 로그로 확인
        Log.d("받아온 친구 정보", "friendUserId: ${friendUserId}, Name: $name, Email: $email, Nickname: $nickname")

        binding.userName.text = nickname

        if (friendUserId != null) {
            // 저장된 팔로우 상태 및 친구 ID 불러오기
            followStatus = getSavedFollowStatus(friendUserId.toString())
            friendFollowId = getSavedFollowId(friendUserId.toString())
            toggleFollowButton(followStatus)
        } else {
            Log.e("FriendpageActivity", "전달된 사용자 데이터가 없습니다.")
        }

        userService = RetrofitClient.create(UserService::class.java, this)
        friendApiService = RetrofitClient.create(FriendApiService::class.java, this)

        setupRecyclerView()
//        userId.toString()?.let { fetchFollowingList(it) }
        setupFollowButton()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMoments.apply {
            layoutManager = LinearLayoutManager(this@FriendpageActivity)
            adapter = momentAdapter
        }
    }

//    private fun fetchFollowingList(userId: String) {
//        friendApiService.getFollowing().enqueue(object : Callback<FriendResponse> {
//            override fun onResponse(call: Call<FriendResponse>, response: Response<FriendResponse>) {
//                if (response.isSuccessful) {
//                    val successData = response.body()?.success
//                    val followingList = if (successData is List<*>) {
//                        successData.filterIsInstance<Friend>()
//                    } else {
//                        emptyList()
//                    }
//                    Log.d("친구 팔로우 목록", "follow ${followingList}")
//                    followStatus = followingList.any { it.id.toString() == userId }
//                    toggleFollowButton(followStatus)  // 초기 상태 설정
//                } else {
//                    Toast.makeText(this@FriendpageActivity, "팔로우 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<FriendResponse>, t: Throwable) {
//                Log.e("FriendpageActivity", "네트워크 오류: ${t.localizedMessage}", t)
//                Toast.makeText(this@FriendpageActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun fetchFriendProfile(userId: String) {
//        userService.getFriendProfile(userId)
//            .enqueue(object : Callback<FriendProfileResponse> {
//                override fun onResponse(call: Call<FriendProfileResponse>, response: Response<FriendProfileResponse>) {
//                    if (response.isSuccessful) {
//                        response.body()?.success?.let { updateProfileUI(it) }
//                    } else {
//                        Toast.makeText(this@FriendpageActivity, "프로필 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<FriendProfileResponse>, t: Throwable) {
//                    Log.e("FriendpageActivity", "네트워크 오류: ${t.localizedMessage}", t)
//                    Toast.makeText(this@FriendpageActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }
//
//    private fun updateProfileUI(friendProfile: FriendProfile) {
//        binding.userName.text = friendProfile.nickname
//        binding.introText.text = friendProfile.introduction
//
//        // 팔로우 상태 설정
//        toggleFollowButton(followStatus)
//    }

    private fun setupFollowButton() {
        binding.followBtn.setOnClickListener {
            if (!binding.followBtn.isSelected) {
                // 팔로우 상태로 변경
                friendApiHelper.sendFriendRequest(friendUserId.toString(), object : FriendRequestCallback {
                    override fun onRequestSuccess(newFriendFollowId: String?) {
                        Log.d("FriendpageActivity", "친구 요청 성공")

                        if (newFriendFollowId != null) {
                            saveFollowId(friendUserId.toString(), newFriendFollowId)
                            saveFollowStatus(friendUserId.toString(), true)
                            toggleFollowButton(true)
                            Toast.makeText(this@FriendpageActivity, "응원하기 성공", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onRequestFailure(message: String) {
                        Log.e("FriendpageActivity", "친구 요청 실패: $message")
                        Toast.makeText(this@FriendpageActivity, message, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // 언팔로우 상태로 변경
                friendFollowId = getSavedFollowId(friendUserId.toString())
                Log.d("friendFollowId", "friendFollowId: ${friendFollowId}")

                friendApiHelper.deleteFollowers(friendFollowId.toString())
                saveFollowStatus(friendUserId.toString(), false)
                toggleFollowButton(false)
            }
        }
    }

    private fun toggleFollowButton(isFollowing: Boolean) {
        binding.followBtn.isSelected = isFollowing  // 셀렉터가 UI 자동 업데이트
        binding.followBtn.text = if (isFollowing) "응원 중" else "응원하기"
    }

    private fun saveFollowStatus(friendUserId: String, isFollowing: Boolean) {
        with(spf.edit()) {
            putBoolean("follow_status_$friendUserId", isFollowing)
            apply()
        }
        Log.d("팔로우 상태 저장", "friendUserId: $friendUserId, 상태: $isFollowing")
    }

    private fun saveFollowId(friendUserId: String, newFollowId: String) {
        with(spf.edit()) {
            putString("follow_id_$friendUserId", newFollowId)
            apply()
        }
        Log.d("팔로우 ID 저장", "friendUserId: $friendUserId, followId: $newFollowId")
    }

    private fun getSavedFollowStatus(friendUserId: String): Boolean {
        return spf.getBoolean("follow_status_$friendUserId", false).also {
            Log.d("팔로우 상태 불러오기", "friendUserId: $friendUserId, 상태: $it")
        }
    }

    private fun getSavedFollowId(friendUserId: String): String? {
        return spf.getString("follow_id_$friendUserId", null).also {
            Log.d("팔로우 ID 불러오기", "friendUserId: $friendUserId, followId: $it")
        }
    }
}
