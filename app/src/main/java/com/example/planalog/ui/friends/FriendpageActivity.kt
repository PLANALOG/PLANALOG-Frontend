package com.example.planalog.ui.friends

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.planalog.R
import com.example.planalog.databinding.ActivityFriendpageBinding
import com.example.planalog.network.api.FriendApiHelper
import com.example.planalog.network.api.FriendCountHelper
import com.example.planalog.network.api.NoticeApiHelper
import com.example.planalog.network.api.UserApiHelper
import com.example.planalog.network.friend.FriendRequestCallback
import com.example.planalog.network.user.response.FriendProfile
import com.example.planalog.ui.post.PostDetailFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FriendpageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendpageBinding
    private var friendUserId: Int? = null
    private var friendFollowId: String? = null
    private var followStatus = false
    private lateinit var spf: SharedPreferences
    private lateinit var friendApiHelper: FriendApiHelper
    private lateinit var userApiHelper: UserApiHelper
    private lateinit var noticeApiHelper: NoticeApiHelper
    private lateinit var friendCountHelper: FriendCountHelper
    private lateinit var momentAdapter: FriendpageMomentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendApiHelper = FriendApiHelper(this)
        noticeApiHelper = NoticeApiHelper(this)
        userApiHelper = UserApiHelper(this)
        friendCountHelper = FriendCountHelper(this)
        spf = getSharedPreferences("follow_status", Context.MODE_PRIVATE)

        // 전달된 JSON 데이터 가져오기
        val userJson = intent.getStringExtra("friendData")
        val userData: Map<String, Any> = Gson().fromJson(userJson, object : TypeToken<Map<String, Any>>() {}.type)

        // ✅ `userId`와 `friendUserId`를 동일하게 설정 (한 번만)
        friendUserId = intent.getIntExtra("userId", -1).takeIf { it != -1 }
            ?: (userData["id"] as? Double)?.toInt()

        val nickname = userData["nickname"] as? String ?: "닉네임 없음"
        binding.userName.text = nickname

        if (friendUserId != null) {
            followStatus = getSavedFollowStatus(friendUserId.toString())
            friendFollowId = getSavedFollowId(friendUserId.toString())
            toggleFollowButton(followStatus)

            fetchFriendCount(friendUserId!!)
            userApiHelper.getFriendProfile(friendUserId!!) { nickname ->
                Log.d("FriendpageActivity", "친구 닉네임 : $nickname")
            }
            fetchFriendMoments()  // ✅ 친구 모먼트 가져오기
        } else {
            Log.e("FriendpageActivity", "전달된 사용자 데이터가 없습니다.")
        }

        setupRecyclerView()
        setupFollowButton()
    }

    override fun onBackPressed() {
        if (binding.mainFrm.visibility == View.VISIBLE) {
            binding.mainFrm.visibility = View.GONE
            supportFragmentManager.popBackStack() // ✅ 뒤로 가기 기능 추가
        } else {
            super.onBackPressed()
        }
    }


    private fun setupRecyclerView() {
        momentAdapter = FriendpageMomentAdapter(this, emptyList())
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
                updatePostCount(moments.size)  // ✅ 친구 모먼트 개수 업데이트
                Log.d("FriendpageActivity", "Fetched moments count: ${moments.size}") // ✅ 모먼트 개수 로그 출력
            },
            onFailure = { errorMessage ->
                Toast.makeText(this@FriendpageActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun openPostDetailFragment(momentId: Int) {
        Log.d("FriendpageActivity", "Opening PostDetailFragment for momentId: $momentId")

        val fragment = PostDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("momentId", momentId)
            }
        }

        val transaction = supportFragmentManager.beginTransaction()
        val frameLayout = findViewById<FrameLayout>(R.id.main_frm)

        frameLayout.visibility = View.VISIBLE // ✅ 프래그먼트가 보이도록 변경
        transaction.replace(R.id.main_frm, fragment)
        transaction.addToBackStack(null) // ✅ 뒤로 가기 가능
        transaction.commit()
    }


    private fun setupFollowButton() {
        binding.followBtn.setOnClickListener {
            if (!binding.followBtn.isSelected) {
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
                friendFollowId = getSavedFollowId(friendUserId.toString())
                Log.d("friendFollowId", "friendFollowId: $friendFollowId")

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
        binding.followBtn.isSelected = isFollowing
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

    private fun fetchFriendCount(userId: Int) {
        friendCountHelper.getUserFriendCount(userId)
    }

    fun updateFriendCountUI(friendCount: Int) {
        binding.friendCountNumber.text = friendCount.toString()
    }

    fun updateFriendProfileUI(friendProfile: FriendProfile) {
        binding.userName.text = friendProfile.nickname
        binding.introText.text = friendProfile.introduction

        Glide.with(this)
            .load(friendProfile.profileImage ?: R.drawable.ic_myprofile)
            .error(R.drawable.ic_myprofile)
            .into(binding.profileImage)
    }

    fun updatePostCount(count: Int) {
        runOnUiThread {
            binding.postCountNumber.text = count.toString()
        }
    }


}
