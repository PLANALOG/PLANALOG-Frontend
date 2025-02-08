package com.example.planalog.ui.friends

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planalog.databinding.FragmentFriendpageBinding
import com.example.planalog.network.RetrofitClient
import com.example.planalog.network.friend.FriendApiService
import com.example.planalog.network.user.response.FriendProfileResponse
import com.example.planalog.network.friend.FriendResponse
import com.example.planalog.network.user.UserService
import com.example.planalog.network.user.response.FriendProfile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendpageFragment : Fragment() {

    private lateinit var binding: FragmentFriendpageBinding
    private lateinit var userService: UserService
    private lateinit var friendApiService: FriendApiService
    private var friendId: Int? = null  // 전달받은 friendId 저장
    private var userId: String? = null  // SharedPreferences에서 불러올 유저 ID (문자열로 변환됨)
    private val momentAdapter by lazy { FriendpageMomentAdapter(emptyList()) }
    private var followStatus = false  // 팔로우 상태

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)
        Log.d("FriendpageFragment", "userId: ${userId}" )

        // SharedPreferences에서 id 가져오기
        friendId = arguments?.getInt("friendId")
        val friendId = arguments?.getInt("friendId")
        if (friendId != null) {
            userId = friendId.toString()  // friendId를 userId로 변환
        } else {
            Toast.makeText(requireContext(), "유효하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendpageBinding.inflate(inflater, container, false)

        friendId?.let {
            Log.d("FriendpageFragment", "전달받은 friendId: $it")
        }
        // Retrofit 서비스 초기화
        userService = RetrofitClient.create(UserService::class.java, requireContext())
        friendApiService = RetrofitClient.create(FriendApiService::class.java, requireContext())


        // 친구 프로필 정보 가져오기
        userId?.let {
            fetchFollowingList(it)  // 팔로우 목록 먼저 가져오기
        }

        // RecyclerView 설정
        setupRecyclerView()

        // 친구 프로필 정보 가져오기
        userId?.let { fetchFriendProfile(it) }

        // 버튼 UI 설정
        setupFollowButton()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMoments.apply {
            layoutManager = LinearLayoutManager(requireContext())
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
                    Toast.makeText(requireContext(), "팔로우 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FriendResponse>, t: Throwable) {
                Log.e("FriendpageFragment", "네트워크 오류: ${t.localizedMessage}", t)
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchFriendProfile(userId: String) {
        userService.getFriendProfile(userId)
            .enqueue(object : Callback<FriendProfileResponse> {
                override fun onResponse(
                    call: Call<FriendProfileResponse>,
                    response: Response<FriendProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val friendProfile = response.body()?.success
                        if (friendProfile != null) {
                            updateProfileUI(friendProfile)
                        } else {
                            val errorMessage = response.body()?.error?.reason ?: "알 수 없는 오류"
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "프로필 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                override fun onFailure(call: Call<FriendProfileResponse>, t: Throwable) {
                    Log.e("FriendpageFragment", "네트워크 오류: ${t.localizedMessage}", t)
                    Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun updateProfileUI(friendProfile: FriendProfile) {
        binding.userName.text = friendProfile.nickname
        binding.introText.text = friendProfile.introduction
        Log.d(
            "FriendpageFragment",
            "프로필 정보: ${friendProfile.nickname}, ${friendProfile.introduction}"
        )
/////////////////////// 미구현상태 수정 전 ///////////////////////
//        binding.postCountNumber.text = friendProfile.momentCount.toString()
//        binding.friendCountNumber.text = friendProfile.friendCount.toString()
//
//        // 프로필 이미지 로드
//        friendProfile.profileImage?.let {
//            Glide.with(this).load(it).into(binding.profileImage)
//        }

//         팔로우 상태에 따른 버튼 상태 설정
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
                Toast.makeText(requireContext(), "응원하기 클릭", Toast.LENGTH_SHORT).show()
            }

            binding.unfollowBtn.setOnClickListener {
                binding.unfollowBtn.visibility = View.GONE
                binding.followBtn.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "응원중 해제", Toast.LENGTH_SHORT).show()
            }
        }
    }


