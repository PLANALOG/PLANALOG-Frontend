package com.example.planalog.ui.friends

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.ActivityFriendpageBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.api.FriendApiHelper
import com.example.planalog.network.api.FriendCountHelper
import com.example.planalog.network.api.NoticeApiHelper
import com.example.planalog.network.friend.FriendRequestCallback
import com.example.planalog.network.friend.FriendService
import com.example.planalog.network.user.FriendCountResponse
import com.example.planalog.network.user.response.FriendProfile
import com.example.planalog.ui.home.notify.NotificationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FriendpageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendpageBinding
    private var friendUserId: Int? = null
    private var friendFollowId : String? = null
    private var followStatus = false  // 팔로우 상태
    private lateinit var spf : SharedPreferences
    private lateinit var friendApiHelper : FriendApiHelper
    private lateinit var noticeApiHelper: NoticeApiHelper
    private lateinit var friendCountHelper: FriendCountHelper
    private lateinit var momentAdapter: FriendpageMomentAdapter  // 어댑터 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendApiHelper = FriendApiHelper(this)
        noticeApiHelper = NoticeApiHelper(this)
        friendCountHelper = FriendCountHelper(this)

        spf = getSharedPreferences("follow_status", Context.MODE_PRIVATE)

        // 전달된 JSON 데이터 가져오기
        val userJson = intent.getStringExtra("friendData")

        // JSON 문자열을 Map<String, Any>로 변환
        val userData: Map<String, Any> = Gson().fromJson(userJson, object : TypeToken<Map<String, Any>>() {}.type)

        // 필요한 정보 추출 예제
        friendUserId = intent.getIntExtra("friendId", -1)
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

        setupRecyclerView()
        if (friendUserId != null) {
            fetchFriendCount(friendUserId!!)
            friendApiHelper.getFriendProfile(friendUserId!!)
            fetchFriendMoments()  // 🔥 친구의 모먼트 불러오기 추가
        }
        setupFollowButton()

    }

    private fun setupRecyclerView() {
        momentAdapter = FriendpageMomentAdapter(this, emptyList())  // 초기 데이터는 빈 리스트
        binding.recyclerViewMoments.apply {
            layoutManager = LinearLayoutManager(this@FriendpageActivity)
            adapter = momentAdapter
        }
    }


    private fun fetchFriendMoments() {
        if (friendUserId == null) return

        friendApiHelper.fetchFriendMoments(friendUserId!!,
            onSuccess = { moments ->
                momentAdapter.updateData(moments)
            },
            onFailure = { errorMessage ->
                Toast.makeText(this@FriendpageActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }


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


//                            val message = "${binding.userName.text}님이 나를 응원하고 싶어 합니다."
//                            val friend = "friend"
//                            val entityId = getNextEntityId()
//
//                            noticeApiHelper.addNotification(message, friend, entityId, onSuccess = { noticeId ->
//
//                                // ✅ 서버 알림 추가가 성공하면 ViewModel에도 추가
//                                val notifyViewModel = NotifyViewModel.getInstance(application)
//                                Log.d("FriendpageActivity", "FriendpageActivity ViewModel 해시코드: ${notifyViewModel.hashCode()}")
//
//                                val notification = NotificationItem(
//                                    userId = friendUserId.toString(),
//                                    noticeId = noticeId,
//                                    profileImageRes = R.drawable.ic_friend_2,
//                                    message = "${binding.userName.text}님이 나를 응원하고 싶어 합니다.",
//                                    onAccept = { userId -> acceptFollowRequest(userId) },
//                                    onReject = { userId, noticeId -> rejectFollowRequest(userId, noticeId) }
//                                )
//
//                                notifyViewModel.addNotification(notification)
//
//                                Log.d("FriendpageActivity", "📌 알림 ViewModel에 전달됨: ${notification.userId}, 메시지: ${notification.message}")
//                            },
//                                onFailure = { errorMessage ->
//                                    Log.e("FriendpageActivity", "📌 서버 알림 추가 실패: $errorMessage")
//                                })
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

                friendApiHelper.deleteFollowers(friendFollowId.toString(), object : FriendRequestCallback {
                    override fun onRequestSuccess(friendId: String?) {
                        Log.d("FriendpageActivity", "친구 삭제 성공")
                        saveFollowStatus(friendUserId.toString(), false)
                        toggleFollowButton(false)
                    }

                    override fun onRequestFailure(message: String) {
                        Log.e("FriendpageActivity", "친구 삭제 실패: $message")
                        Toast.makeText(this@FriendpageActivity, message, Toast.LENGTH_SHORT).show()
                    }

                })

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

    private fun acceptFollowRequest(userId: String) {
        Toast.makeText(this, "$userId 응원 수락", Toast.LENGTH_SHORT).show()
        // TODO: API 호출하여 팔로우 수락 처리
    }

    private fun rejectFollowRequest(userId: String, noticeId : String) {
        Toast.makeText(this, "$userId 응원 거절", Toast.LENGTH_SHORT).show()
        // TODO: API 호출하여 팔로우 거절 처리
    }

    private fun getNextEntityId(): Int {
        val sharedPreferences = getSharedPreferences("entity_prefs", Context.MODE_PRIVATE)
        val currentId = sharedPreferences.getInt("entityId", 2) // 기본값 0
        val newId = currentId + 1

        // 증가한 entityId 저장
        sharedPreferences.edit().putInt("entityId", newId).apply()
        return newId
    }

    private fun resetFollowInfo() {
        with(spf.edit()) {
            remove("follow_status_$friendUserId") // 팔로우 상태 삭제
            remove("follow_id_$friendUserId")    // 팔로우 ID 삭제
            apply()
        }
        followStatus = false
        friendFollowId = null
        toggleFollowButton(false) // UI 업데이트
        Log.d("FriendpageActivity", "📌 팔로우 정보 초기화 완료")
    }

    private fun fetchFriendCount(userId: Int) {
        friendCountHelper.getUserFriendCount(userId)
    }

    fun updateFriendCountUI(friendCount: Int) {
        binding.friendCountNumber.text = friendCount.toString()
    }

    fun updateFriendProfileUI(friendProfile: FriendProfile) {
        binding.userName.text = friendProfile.nickname
        binding.introText.text = friendProfile.introduction

        // Glide를 사용하여 프로필 이미지 로드
        Glide.with(this)
            .load(friendProfile.profileImage ?: R.drawable.ic_myprofile)
            .error(R.drawable.ic_myprofile)
            .into(binding.profileImage)
    }
}